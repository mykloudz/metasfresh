/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution *
 * Copyright (C) 2008 SC ARHIPAC SERVICE SRL. All Rights Reserved. *
 * This program is free software; you can redistribute it and/or modify it *
 * under the terms version 2 of the GNU General Public License as published *
 * by the Free Software Foundation. This program is distributed in the hope *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. *
 * See the GNU General Public License for more details. *
 * You should have received a copy of the GNU General Public License along *
 * with this program; if not, write to the Free Software Foundation, Inc., *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA. *
 *****************************************************************************/
package org.adempiere.exceptions;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;

import org.adempiere.ad.service.IDeveloperModeBL;
import org.adempiere.util.logging.LoggingHelper;
import org.compiere.model.Null;
import org.compiere.util.Env;
import org.slf4j.Logger;

import com.google.common.collect.ImmutableMap;

import ch.qos.logback.classic.Level;
import de.metas.error.AdIssueId;
import de.metas.i18n.IMsgBL;
import de.metas.i18n.ITranslatableString;
import de.metas.i18n.Language;
import de.metas.i18n.TranslatableStringBuilder;
import de.metas.i18n.TranslatableStrings;
import de.metas.logging.MetasfreshLastError;
import de.metas.util.Services;
import lombok.NonNull;

/**
 * Any exception that occurs inside the Adempiere core
 *
 * @author Teo Sarca, SC ARHIPAC SERVICE SRL
 */
public class AdempiereException extends RuntimeException
		implements IIssueReportableAware
{
	/**
	 *
	 */
	private static final long serialVersionUID = -1813037338765245293L;

	/**
	 * Wraps given <code>throwable</code> as {@link AdempiereException}, if it's not already an {@link AdempiereException}.<br>
	 * Note that this method also tries to pick the most specific adempiere exception (work in progress).
	 *
	 * @param throwable
	 * @return {@link AdempiereException} or <code>null</code> if the throwable was null.
	 */
	public static AdempiereException wrapIfNeeded(final Throwable throwable)
	{
		if (throwable == null)
		{
			return null;
		}

		final Throwable cause = extractCause(throwable);

		if (cause instanceof AdempiereException)
		{
			return (AdempiereException)cause;
		}

		if (cause instanceof SQLException)
		{
			return DBException.wrapIfNeeded(cause);
		}

		if (cause != throwable)
		{
			return wrapIfNeeded(cause);
		}

		// default
		return new AdempiereException(throwable.getClass().getSimpleName() + ": " + extractMessage(cause), cause);
	}

	/**
	 * Extracts throwable message.
	 *
	 * @param throwable
	 * @return message; never return null
	 */
	public static final String extractMessage(final Throwable throwable)
	{
		// guard against NPE, shall not happen
		if (throwable == null)
		{
			return "null";
		}

		if (throwable instanceof NullPointerException)
		{
			return throwable.toString();
		}
		else
		{
			String message = throwable.getLocalizedMessage();

			// If throwable message is null or it's very short then it's better to use throwable.toString()
			if (message == null || message.length() < 4)
			{
				message = throwable.toString();
			}

			return message;
		}
	}

	public static final ITranslatableString extractMessageTrl(final Throwable throwable)
	{
		if (throwable instanceof AdempiereException)
		{
			final AdempiereException ex = (AdempiereException)throwable;
			return ex.getMessageBuilt();
		}

		return TranslatableStrings.constant(extractMessage(throwable));
	}

	public static final Map<String, Object> extractParameters(final Throwable throwable)
	{
		if (throwable instanceof AdempiereException)
		{
			return ((AdempiereException)throwable).getParameters();
		}
		else
		{
			return ImmutableMap.of();
		}
	}

	/**
	 * Extract cause exception from those exceptions which are only about wrapping the real cause (e.g. ExecutionException, InvocationTargetException).
	 *
	 * @param throwable
	 * @return cause or throwable; never returns null
	 */
	public static final Throwable extractCause(final Throwable throwable)
	{
		final Throwable cause = throwable.getCause();
		if (cause == null)
		{
			return throwable;
		}

		if (throwable instanceof ExecutionException)
		{
			return cause;
		}
		if (throwable instanceof com.google.common.util.concurrent.UncheckedExecutionException)
		{
			return cause;
		}

		if (cause instanceof NullPointerException)
		{
			return cause;
		}
		if (cause instanceof IllegalArgumentException)
		{
			return cause;
		}
		if (cause instanceof IllegalStateException)
		{
			return cause;
		}

		if (throwable instanceof InvocationTargetException)
		{
			return cause;
		}

		return throwable;
	}

	/**
	 * Convenient method to suppress a given exception if there is an already main exception which is currently thrown.
	 *
	 * @param exceptionToSuppress
	 * @param mainException
	 * @throws AdempiereException if mainException was null. It will actually be the exceptionToSuppress, wrapped to AdempiereException if it was needed.
	 */
	public static final void suppressOrThrow(final Throwable exceptionToSuppress, final Throwable mainException)
	{
		if (mainException == null)
		{
			throw wrapIfNeeded(exceptionToSuppress);
		}
		else
		{
			mainException.addSuppressed(exceptionToSuppress);
		}
	}

	/**
	 * If enabled, the language used to translate the error message is captured when the exception is constructed.
	 *
	 * If is NOT enabled, the language used to translate the error message is acquired when the message is translated.
	 */
	public static final void enableCaptureLanguageOnConstructionTime()
	{
		AdempiereException.captureLanguageOnConstructionTime = true;
	}

	public static AdempiereException ofADMessage(final String adMessage, final Object... msgParameters)
	{
		final ITranslatableString message = Services.get(IMsgBL.class).getTranslatableMsgText(adMessage, msgParameters);
		return new AdempiereException(message);
	}

	private static boolean captureLanguageOnConstructionTime = false;

	private final ITranslatableString messageTrl;
	/** Build message but not translated */
	private ITranslatableString _messageBuilt = null;
	private final String adLanguage;

	private AdIssueId adIssueId = null;
	private boolean userNotified = false;
	private boolean userValidationError;

	private Map<String, Object> parameters = null;
	private boolean appendParametersToMessage = false;

	/**
	 * Default Constructor (saved logger error will be used as message)
	 */
	@Deprecated
	public AdempiereException()
	{
		this(getMessageFromLogger());
	}

	public AdempiereException(final String message)
	{
		this.adLanguage = captureLanguageOnConstructionTime ? Env.getAD_Language() : null;
		this.messageTrl = Services.get(IMsgBL.class).parseTranslatableString(message);
	}

	public AdempiereException(@NonNull final ITranslatableString message)
	{
		this.adLanguage = captureLanguageOnConstructionTime ? Env.getAD_Language() : null;
		this.messageTrl = message;
	}

	public AdempiereException(final String adLanguage, final String adMessage, final Object[] params)
	{
		this.messageTrl = Services.get(IMsgBL.class).getTranslatableMsgText(adMessage, params);
		this.adLanguage = captureLanguageOnConstructionTime ? adLanguage : null;

		setParameter("AD_Language", this.adLanguage);
		setParameter("AD_Message", adMessage);
	}

	public AdempiereException(final String adMessage, final Object[] params)
	{
		this(Env.getAD_Language(), adMessage, params);
	}

	public AdempiereException(@Nullable final Throwable cause)
	{
		super(cause);
		this.adLanguage = captureLanguageOnConstructionTime ? Env.getAD_Language() : null;
		this.messageTrl = TranslatableStrings.empty();
	}

	public AdempiereException(final String message, final Throwable cause)
	{
		super(cause);
		this.adLanguage = captureLanguageOnConstructionTime ? Env.getAD_Language() : null;
		this.messageTrl = TranslatableStrings.constant(message);
	}

	public AdempiereException(@NonNull final ITranslatableString message, final Throwable cause)
	{
		super(cause);
		this.adLanguage = captureLanguageOnConstructionTime ? Env.getAD_Language() : null;
		this.messageTrl = message;
	}

	/**
	 * Gets original message
	 *
	 * @return original message
	 */
	protected final ITranslatableString getOriginalMessage()
	{
		return messageTrl;
	}

	@Override
	public final String getLocalizedMessage()
	{
		final ITranslatableString message = getMessageBuilt();

		if (!Language.isBaseLanguageSet())
		{
			return message.getDefaultValue();
		}

		try
		{
			final String adLanguage = getADLanguage();
			return message.translate(adLanguage);
		}
		catch (final Throwable ex)
		{
			// don't fail while building the actual exception
			ex.printStackTrace();
			return message.getDefaultValue();
		}
	}

	@Override
	public final String getMessage()
	{
		// always return the localized string,
		// else those APIs which are using getMessage() will fetch the not so nice text message.
		return getLocalizedMessage();
	}

	/**
	 * Reset the build message. Next time when the message is needed, it will be re-builded first ({@link #buildMessage()}).
	 *
	 * Call this method from each setter which would change your message.
	 */
	protected final void resetMessageBuilt()
	{
		this._messageBuilt = null;
	}

	private final ITranslatableString getMessageBuilt()
	{
		ITranslatableString messageBuilt = _messageBuilt;
		if (messageBuilt == null)
		{
			_messageBuilt = messageBuilt = buildMessage();
		}
		return messageBuilt;
	}

	/**
	 * Build error message (if needed) and return it.
	 *
	 * By default this method is returning initial message, but extending classes could override it.
	 *
	 * WARNING: to avoid recursion, please never ever call {@link #getMessage()} or {@link #getLocalizedMessage()} but
	 * <ul>
	 * <li>call {@link #getOriginalMessage()}
	 * <li>or store the error message in a separate field and use it</li>
	 *
	 * @return built detail message
	 */
	protected ITranslatableString buildMessage()
	{
		final TranslatableStringBuilder message = TranslatableStrings.builder();
		message.append(getOriginalMessage());
		if (appendParametersToMessage)
		{
			appendParameters(message);
		}
		return message.build();
	}

	protected final String getADLanguage()
	{
		return adLanguage != null ? adLanguage : Env.getAD_Language();
	}

	/**
	 * @return error message from logger
	 * @see MetasfreshLastError#retrieveError()
	 */
	private static String getMessageFromLogger()
	{
		//
		// Check last error
		final org.compiere.util.ValueNamePair err = MetasfreshLastError.retrieveError();
		String msg = null;
		if (err != null)
		{
			msg = err.getName();
		}

		//
		// Check last exception
		if (msg == null)
		{
			final Throwable ex = MetasfreshLastError.retrieveException();
			if (ex != null)
			{
				msg = ex.getLocalizedMessage();
			}
		}

		//
		// Fallback: no last error found => use Unknown error message
		if (msg == null)
		{
			msg = "UnknownError";
		}

		return msg;
	}

	/**
	 * Convenient method to throw this exception or just log it as {@link Level#ERROR}.
	 *
	 * @param throwIt <code>true</code> if the exception shall be thrown
	 * @param logger
	 * @return always returns <code>false</code>.
	 */
	public final boolean throwOrLogSevere(final boolean throwIt, final Logger logger)
	{
		return throwOrLog(throwIt, Level.ERROR, logger);
	}

	/**
	 * Convenient method to throw this exception or just log it as {@link Level#WARN}.
	 *
	 * @param throwIt <code>true</code> if the exception shall be thrown
	 * @param logger
	 * @return always returns <code>false</code>.
	 */
	public final boolean throwOrLogWarning(final boolean throwIt, final Logger logger)
	{
		return throwOrLog(throwIt, Level.WARN, logger);
	}

	/**
	 * Convenient method to throw this exception if developer mode is enabled or just log it as {@link Level#WARNING}.
	 *
	 * @param logger
	 * @return always returns <code>false</code>.
	 */
	public final boolean throwIfDeveloperModeOrLogWarningElse(final Logger logger)
	{
		final boolean throwIt = Services.get(IDeveloperModeBL.class).isEnabled();
		return throwOrLog(throwIt, Level.WARN, logger);
	}

	private final boolean throwOrLog(final boolean throwIt, final Level logLevel, final Logger logger)
	{
		if (throwIt)
		{
			throw this;
		}
		else if (logger != null)
		{
			LoggingHelper.log(logger, logLevel, "this is logged, no Exception thrown (throwIt=false, logger!=null):", this);
			LoggingHelper.log(logger, logLevel, getLocalizedMessage(), this);
			return false;
		}
		else
		{
			System.err.println(this.getClass().getSimpleName() + "throwOrLog: this is written to std-err, no Exception thrown (throwIt=false, logger=null):");
			this.printStackTrace();
			return false;
		}
	}

	/**
	 * If developer mode is active, it logs a warning with given exception.
	 *
	 * If the developer mode is not active, this method does nothing
	 *
	 * @param logger
	 * @param exceptionSupplier {@link AdempiereException} supplier
	 */
	public static final void logWarningIfDeveloperMode(final Logger logger, Supplier<? extends AdempiereException> exceptionSupplier)
	{
		if (!Services.get(IDeveloperModeBL.class).isEnabled())
		{
			return;
		}

		final boolean throwIt = false;
		final AdempiereException exception = exceptionSupplier.get();
		exception.throwOrLog(throwIt, Level.WARN, logger);
	}

	@Override
	public final void markIssueReported(final AdIssueId adIssueId)
	{
		this.adIssueId = adIssueId;
	}

	@Override
	public final boolean isIssueReported()
	{
		return adIssueId != null;
	}

	@Override
	public AdIssueId getAdIssueId()
	{
		return adIssueId;
	}

	public final boolean isUserNotified()
	{
		return userNotified;
	}

	public final AdempiereException markUserNotified()
	{
		userNotified = true;
		return this;
	}

	/**
	 * Sets parameter.
	 *
	 * @param name parameter name
	 * @param value parameter value; {@code null} is added as {@link Null}
	 */
	@OverridingMethodsMustInvokeSuper
	public AdempiereException setParameter(@NonNull final String name, @Nullable final Object value)
	{
		if (parameters == null)
		{
			parameters = new LinkedHashMap<>();
		}

		parameters.put(name, Null.box(value));
		resetMessageBuilt();

		return this;
	}

	protected final AdempiereException putParametetersFrom(@Nullable final Throwable ex)
	{
		if (ex instanceof AdempiereException)
		{
			this.putParameteters(((AdempiereException)ex).parameters);
		}

		return this;
	}

	private final AdempiereException putParameteters(@Nullable final Map<String, Object> parameters)
	{
		if (parameters == null || parameters.isEmpty())
		{
			return this;
		}

		if (this.parameters == null)
		{
			this.parameters = new LinkedHashMap<>();
		}

		parameters.forEach((name, value) -> this.parameters.put(name, Null.box(value)));
		resetMessageBuilt();

		return this;
	}

	@OverridingMethodsMustInvokeSuper
	public <T extends Enum<?>> AdempiereException setParameter(@NonNull final T enumValue)
	{
		final String parameterName = buildParameterName(enumValue);
		return setParameter(parameterName, enumValue);
	}

	@OverridingMethodsMustInvokeSuper
	public <T extends Enum<?>> boolean hasParameter(@NonNull final T enumValue)
	{
		final String parameterName = buildParameterName(enumValue);
		return enumValue.equals(getParameter(parameterName));
	}

	private static final <T extends Enum<?>> String buildParameterName(@NonNull final T enumValue)
	{
		return enumValue.getClass().getSimpleName();
	}

	public final boolean hasParameter(@NonNull final String name)
	{
		return parameters == null ? false : parameters.containsKey(name);
	}

	public final Object getParameter(@NonNull final String name)
	{
		return parameters != null ? parameters.get(name) : null;
	}

	public final Map<String, Object> getParameters()
	{
		if (parameters == null)
		{
			return ImmutableMap.of();
		}
		return ImmutableMap.copyOf(parameters);
	}

	/**
	 * Ask the exception to also include the parameters in it's message.
	 */
	@OverridingMethodsMustInvokeSuper
	public AdempiereException appendParametersToMessage()
	{
		if (!appendParametersToMessage)
		{
			appendParametersToMessage = true;
			resetMessageBuilt();
		}
		return this;
	}

	/**
	 * Utility method that can be used by both external callers and subclasses'
	 * {@link AdempiereException#buildMessage()} or
	 * {@link #getMessage()} methods to create a string from this instance's parameters.
	 *
	 * Note: as of now, this method is final by intention; if you need the returned string to be customized, I suggest to not override this method somewhere,
	 * but instead add another method that can take a format string as parameter.
	 *
	 * @return an empty string if this instance has no parameters or otherwise something like
	 *
	 *         <pre>
	 * Additional parameters:
	 * name1: value1
	 * name2: value2
	 *         </pre>
	 */
	protected final ITranslatableString buildParametersString()
	{
		final Map<String, Object> parameters = getParameters();
		if (parameters.isEmpty())
		{
			return TranslatableStrings.empty();
		}

		final TranslatableStringBuilder message = TranslatableStrings.builder();
		message.append("Additional parameters:");
		for (final Map.Entry<String, Object> paramName2Value : parameters.entrySet())
		{
			message.append("\n ").append(paramName2Value.getKey()).append(": ").appendObj(paramName2Value.getValue());
		}

		return message.build();
	}

	/**
	 * Utility method to convert parameters to string and append them to given <code>message</code>
	 *
	 * @see #buildParametersString()
	 */
	protected final void appendParameters(final TranslatableStringBuilder message)
	{
		final ITranslatableString parametersStr = buildParametersString();
		if (TranslatableStrings.isBlank(parametersStr))
		{
			return;
		}

		if (!message.isEmpty())
		{
			message.append("\n");
		}
		message.append(parametersStr);
	}

	/**
	 * Marks this exception as user validation error.
	 * In case an exception is a user validation error, the framework assumes the message is user friendly and can be displayed directly.
	 * More, the webui auto-saving will not hide/ignore this error put it will propagate it directly to user.
	 */
	@OverridingMethodsMustInvokeSuper
	public AdempiereException markAsUserValidationError()
	{
		userValidationError = true;
		return this;
	}

	public final boolean isUserValidationError()
	{
		return userValidationError;
	}

	public static final boolean isUserValidationError(final Throwable ex)
	{
		return (ex instanceof AdempiereException) && ((AdempiereException)ex).isUserValidationError();
	}

	/**
	 * Fluent version of {@link #addSuppressed(Throwable)}
	 */
	public AdempiereException suppressing(@NonNull final Throwable exception)
	{
		addSuppressed(exception);
		return this;
	}
}
