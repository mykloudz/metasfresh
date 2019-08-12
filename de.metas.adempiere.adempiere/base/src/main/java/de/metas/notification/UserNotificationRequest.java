package de.metas.notification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.adempiere.ad.element.api.AdWindowId;
import org.adempiere.exceptions.AdempiereException;
import org.adempiere.util.lang.impl.TableRecordReference;
import org.springframework.core.io.Resource;

import com.google.common.collect.ImmutableList;

import de.metas.event.EventBusConstants;
import de.metas.event.Topic;
import de.metas.user.UserId;
import de.metas.util.Check;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;

/*
 * #%L
 * de.metas.adempiere.adempiere.base
 * %%
 * Copyright (C) 2018 metas GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation,
 * either version 2 of the
 * License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program. If not,
 * see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

@Value
public class UserNotificationRequest
{
	Recipient recipient;
	UserNotificationsConfig notificationsConfig;

	private static final NotificationGroupName DEFAULT_NotificationGroupName = NotificationGroupName.of(EventBusConstants.TOPIC_GeneralUserNotifications.getName());
	NotificationGroupName notificationGroupName;

	boolean important;

	/** Optional; takes precedence over {@link #subjectADMessage}, if set. */
	String subjectPlain;

	/** Optional */
	String subjectADMessage;
	List<Object> subjectADMessageParams;

	/** Optional; takes precedence over {@link #contentADMessage}, if set. */
	String contentPlain;

	/** Optional */
	String contentADMessage;
	List<Object> contentADMessageParams;

	TargetAction targetAction;

	List<Resource> attachments;

	// Options
	boolean noEmail;

	@Builder(toBuilder = true)
	private UserNotificationRequest(
			final Recipient recipient,
			final UserNotificationsConfig notificationsConfig,
			//
			final NotificationGroupName notificationGroupName,
			//
			final boolean important,
			//
			@Nullable final String subjectPlain,
			@Nullable final String subjectADMessage,
			@Singular final List<Object> subjectADMessageParams,
			//
			final String contentPlain,
			final String contentADMessage,
			@Singular final List<Object> contentADMessageParams,
			//
			@Nullable TargetAction targetAction,
			//
			@Singular final List<Resource> attachments,
			// Options:
			final boolean noEmail)
	{
		this.notificationsConfig = notificationsConfig;

		if (recipient == null)
		{
			if (notificationsConfig != null)
			{
				this.recipient = Recipient.user(notificationsConfig.getUserId());
			}
			else
			{
				throw new AdempiereException("notificationConfig or recipient shall be specified");
			}
		}
		else if (recipient.isAllUsers())
		{
			this.recipient = recipient;
		}
		else
		{
			this.recipient = recipient;
		}

		this.notificationGroupName = notificationGroupName != null ? notificationGroupName : DEFAULT_NotificationGroupName;

		this.important = important;

		this.subjectPlain = subjectPlain;
		this.subjectADMessage = subjectADMessage;
		this.subjectADMessageParams = copyADMessageParams(subjectADMessageParams);

		this.contentPlain = contentPlain;
		this.contentADMessage = contentADMessage;
		this.contentADMessageParams = copyADMessageParams(contentADMessageParams);

		this.targetAction = targetAction;

		this.attachments = ImmutableList.copyOf(attachments);

		this.noEmail = noEmail;
	}

	private static List<Object> copyADMessageParams(final List<Object> params)
	{
		return params != null && !params.isEmpty() ? Collections.unmodifiableList(new ArrayList<>(params)) : ImmutableList.of();
	}

	public String getADLanguageOrGet(final Supplier<String> defaultLanguageSupplier)
	{
		return getNotificationsConfig().getUserADLanguageOrGet(defaultLanguageSupplier);
	}

	public String getSubjectADMessageOr(final String defaultValue)
	{
		return !Check.isEmpty(subjectADMessage) ? subjectADMessage : defaultValue;
	}

	public UserNotificationRequest deriveByNotificationsConfig(@NonNull final UserNotificationsConfig notificationsConfig)
	{
		if (Objects.equals(this.notificationsConfig, notificationsConfig))
		{
			return this;
		}

		return toBuilder().notificationsConfig(notificationsConfig).build();
	}

	public UserNotificationRequest deriveByRecipient(@NonNull final Recipient recipient)
	{
		if (Objects.equals(this.recipient, recipient))
		{
			return this;
		}

		return toBuilder().recipient(recipient).notificationsConfig(null).build();
	}

	//
	//
	//
	//
	//

	public static class UserNotificationRequestBuilder
	{
		public UserNotificationRequestBuilder recipientUserId(@NonNull final UserId userId)
		{
			return recipient(Recipient.user(userId));
		}

		public UserNotificationRequestBuilder topic(final Topic topic)
		{
			return notificationGroupName(NotificationGroupName.of(topic));
		}
	}

	public interface TargetAction
	{
	}

	@lombok.Value
	@lombok.Builder(toBuilder = true)
	public static class TargetRecordAction implements TargetAction
	{
		public static TargetRecordAction of(@NonNull final TableRecordReference record)
		{
			return builder().record(record).build();
		}

		public static TargetRecordAction ofRecordAndWindow(@NonNull final TableRecordReference record, final int adWindowId)
		{
			return builder().record(record).adWindowId(AdWindowId.optionalOfRepoId(adWindowId)).build();
		}

		public static TargetRecordAction ofRecordAndWindow(@NonNull final TableRecordReference record, @NonNull final AdWindowId adWindowId)
		{
			return builder().record(record).adWindowId(Optional.of(adWindowId)).build();
		}

		public static TargetRecordAction of(@NonNull final String tableName, final int recordId)
		{
			return of(TableRecordReference.of(tableName, recordId));
		}

		public static TargetRecordAction cast(final TargetAction targetAction)
		{
			return (TargetRecordAction)targetAction;
		}

		@NonNull
		@Builder.Default
		Optional<AdWindowId> adWindowId = Optional.empty();

		@NonNull
		TableRecordReference record;

		String recordDisplayText;
	}

	@lombok.Value
	@lombok.Builder
	public static class TargetViewAction implements TargetAction
	{
		public static final TargetViewAction cast(final TargetAction targetAction)
		{
			return (TargetViewAction)targetAction;
		}

		int adWindowId;
		@NonNull
		String viewId;
	}
}
