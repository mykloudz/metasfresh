package de.metas.vertical.healthcare_ch.forum_datenaustausch_ch.base.export.invoice;

import static de.metas.util.Check.assumeNotNull;
import static java.math.BigDecimal.ZERO;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Base64;
import java.util.GregorianCalendar;
import java.util.List;

import javax.annotation.Nullable;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.util.MimeType;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.Maps;

import de.metas.invoice_gateway.spi.CustomInvoicePayload;
import de.metas.invoice_gateway.spi.InvoiceExportClient;
import de.metas.invoice_gateway.spi.esr.model.ESRPaymentInfo;
import de.metas.invoice_gateway.spi.model.AddressInfo;
import de.metas.invoice_gateway.spi.model.InvoiceAttachment;
import de.metas.invoice_gateway.spi.model.InvoiceExportResult;
import de.metas.invoice_gateway.spi.model.InvoiceLine;
import de.metas.invoice_gateway.spi.model.InvoiceTax;
import de.metas.invoice_gateway.spi.model.MetasfreshVersion;
import de.metas.invoice_gateway.spi.model.Money;
import de.metas.invoice_gateway.spi.model.PersonInfo;
import de.metas.invoice_gateway.spi.model.export.InvoiceToExport;
import de.metas.util.Check;
import de.metas.util.collections.CollectionUtils;
import de.metas.util.xml.XmlIntrospectionUtil;
import de.metas.vertical.healthcare_ch.forum_datenaustausch_ch.base.CrossVersionServiceRegistry;
import de.metas.vertical.healthcare_ch.forum_datenaustausch_ch.base.Types.RequestType;
import de.metas.vertical.healthcare_ch.forum_datenaustausch_ch.base.config.ExportConfig;
import de.metas.vertical.healthcare_ch.forum_datenaustausch_ch.commons.ForumDatenaustauschChConstants;
import de.metas.vertical.healthcare_ch.forum_datenaustausch_ch.commons.XmlMode;
import de.metas.vertical.healthcare_ch.forum_datenaustausch_ch.invoice_xversion.CrossVersionRequestConverter;
import de.metas.vertical.healthcare_ch.forum_datenaustausch_ch.invoice_xversion.model.commontypes.XmlCompany;
import de.metas.vertical.healthcare_ch.forum_datenaustausch_ch.invoice_xversion.model.commontypes.XmlInvoice.InvoiceMod;
import de.metas.vertical.healthcare_ch.forum_datenaustausch_ch.invoice_xversion.model.commontypes.XmlPerson;
import de.metas.vertical.healthcare_ch.forum_datenaustausch_ch.invoice_xversion.model.commontypes.XmlPostal;
import de.metas.vertical.healthcare_ch.forum_datenaustausch_ch.invoice_xversion.request.model.XmlPayload;
import de.metas.vertical.healthcare_ch.forum_datenaustausch_ch.invoice_xversion.request.model.XmlPayload.PayloadMod;
import de.metas.vertical.healthcare_ch.forum_datenaustausch_ch.invoice_xversion.request.model.XmlProcessing.ProcessingMod;
import de.metas.vertical.healthcare_ch.forum_datenaustausch_ch.invoice_xversion.request.model.XmlRequest;
import de.metas.vertical.healthcare_ch.forum_datenaustausch_ch.invoice_xversion.request.model.XmlRequest.RequestMod;
import de.metas.vertical.healthcare_ch.forum_datenaustausch_ch.invoice_xversion.request.model.payload.XmlBody;
import de.metas.vertical.healthcare_ch.forum_datenaustausch_ch.invoice_xversion.request.model.payload.XmlBody.BodyMod;
import de.metas.vertical.healthcare_ch.forum_datenaustausch_ch.invoice_xversion.request.model.payload.body.XmlBalance.BalanceMod;
import de.metas.vertical.healthcare_ch.forum_datenaustausch_ch.invoice_xversion.request.model.payload.body.XmlDocument;
import de.metas.vertical.healthcare_ch.forum_datenaustausch_ch.invoice_xversion.request.model.payload.body.XmlEsr;
import de.metas.vertical.healthcare_ch.forum_datenaustausch_ch.invoice_xversion.request.model.payload.body.XmlProlog.PrologMod;
import de.metas.vertical.healthcare_ch.forum_datenaustausch_ch.invoice_xversion.request.model.payload.body.XmlService;
import de.metas.vertical.healthcare_ch.forum_datenaustausch_ch.invoice_xversion.request.model.payload.body.XmlService.ServiceModWithSelector;
import de.metas.vertical.healthcare_ch.forum_datenaustausch_ch.invoice_xversion.request.model.payload.body.XmlService.ServiceModWithSelector.ServiceModWithSelectorBuilder;
import de.metas.vertical.healthcare_ch.forum_datenaustausch_ch.invoice_xversion.request.model.payload.body.esr.XmlBank;
import de.metas.vertical.healthcare_ch.forum_datenaustausch_ch.invoice_xversion.request.model.payload.body.esr.XmlEsr9;
import de.metas.vertical.healthcare_ch.forum_datenaustausch_ch.invoice_xversion.request.model.payload.body.prolog.XmlSoftware.SoftwareMod;
import de.metas.vertical.healthcare_ch.forum_datenaustausch_ch.invoice_xversion.request.model.payload.body.vat.XmlVat.VatMod;
import de.metas.vertical.healthcare_ch.forum_datenaustausch_ch.invoice_xversion.request.model.payload.body.vat.XmlVat.VatMod.VatModBuilder;
import de.metas.vertical.healthcare_ch.forum_datenaustausch_ch.invoice_xversion.request.model.payload.body.vat.XmlVatRate;
import de.metas.vertical.healthcare_ch.forum_datenaustausch_ch.invoice_xversion.request.model.processing.XmlTransport.TransportMod;
import lombok.NonNull;

/*
 * #%L
 * vertical-healthcare_ch.invoice_gateway.forum_datenaustausch_ch.invoice_commons
 * %%
 * Copyright (C) 2018 metas GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

public class InvoiceExportClientImpl implements InvoiceExportClient
{
	private final CrossVersionServiceRegistry crossVersionServiceRegistry;
	private final CrossVersionRequestConverter exportConverter;
	private final XmlMode exportFileMode;
	private final String exportFileFromEAN;
	private final String exportFileViaEAN;

	public InvoiceExportClientImpl(
			@NonNull final CrossVersionServiceRegistry crossVersionServiceRegistry,
			@NonNull final ExportConfig exportConfig)
	{
		this.crossVersionServiceRegistry = crossVersionServiceRegistry;
		exportConverter = crossVersionServiceRegistry.getRequestConverterForSimpleVersionName(exportConfig.getXmlVersion());
		exportFileMode = assumeNotNull(exportConfig.getMode(), "The given exportConfig needs to have a non-null mode; exportconfig={}", exportConfig);
		exportFileFromEAN = exportConfig.getFromEAN();
		exportFileViaEAN = exportConfig.getViaEAN();
	}

	@Override
	public boolean canExport(@NonNull final InvoiceToExport invoice)
	{
		final ImmutableMultimap<CrossVersionRequestConverter, InvoiceAttachment> //
		converters = extractConverters(invoice.getInvoiceAttachments());

		// TODO check if
		// * the invoice's language and currency is OK, and if the invoice has a supported XML attachment
		// * ..and if all the invoice's lines have exactly *one* externalId that matches a serviceRecord from the XML attachment
		return !converters.isEmpty();
	}

	@Override
	public List<InvoiceExportResult> export(@NonNull final InvoiceToExport invoice)
	{
		final ImmutableMultimap<CrossVersionRequestConverter, InvoiceAttachment> //
		converter2ConvertableAttachment = extractConverters(invoice.getInvoiceAttachments());

		final ImmutableList.Builder<InvoiceExportResult> exportResults = ImmutableList.builder();

		for (final CrossVersionRequestConverter importConverter : converter2ConvertableAttachment.keySet())
		{
			for (final InvoiceAttachment attachment : converter2ConvertableAttachment.get(importConverter))
			{
				final XmlRequest xRequest = importConverter.toCrossVersionRequest(attachment.getDataAsInputStream());
				final XmlRequest xAugmentedRequest = augmentRequest(xRequest, invoice);

				final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				exportConverter.fromCrossVersionRequest(xAugmentedRequest, outputStream);

				final ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

				final InvoiceExportResult exportResult = InvoiceExportResult.builder()
						.data(inputStream)
						.fileName("Export_" + attachment.getFileName())
						.mimeType(attachment.getMimeType())
						.invoiceExportProviderId(ForumDatenaustauschChConstants.INVOICE_EXPORT_PROVIDER_ID)
						.recipientId(invoice.getRecipient().getId())
						.build();

				exportResults.add(exportResult);
			}
		}

		return exportResults.build();
	}

	private ImmutableMultimap<CrossVersionRequestConverter, InvoiceAttachment> extractConverters(
			@NonNull final List<InvoiceAttachment> invoiceAttachments)
	{
		final Builder<CrossVersionRequestConverter, InvoiceAttachment> //
		result = ImmutableMultimap.<CrossVersionRequestConverter, InvoiceAttachment> builder();

		for (final InvoiceAttachment attachment : invoiceAttachments)
		{
			if (!MimeType.TYPE_XML.equals(attachment.getMimeType()))
			{
				continue;
			}

			final String xsdName = XmlIntrospectionUtil.extractXsdValueOrNull(attachment.getDataAsInputStream());

			final CrossVersionRequestConverter converter = crossVersionServiceRegistry.getRequestConverterForXsdName(xsdName);
			if (converter == null)
			{
				continue;
			}
			result.put(converter, attachment);
		}

		return result.build();
	}

	private XmlRequest augmentRequest(
			@NonNull final XmlRequest xRequest,
			@NonNull final InvoiceToExport invoice)
	{
		final RequestMod requestMod = RequestMod
				.builder()
				.modus(exportFileMode)
				.processingMod(createProcessingMod())
				.payloadMod(createPayloadMod(invoice, xRequest.getPayload()))
				.build();

		return xRequest.withMod(requestMod);
	}

	private ProcessingMod createProcessingMod()
	{
		return ProcessingMod.builder()
				.transportMod(createTransportMod())
				.build();
	}

	private TransportMod createTransportMod()
	{
		if (Check.isEmpty(exportFileFromEAN, true))
		{
			return null;
		}
		return TransportMod.builder()
				.from(exportFileFromEAN)
				.replacementViaEAN(exportFileViaEAN)
				.build();
	}

	private PayloadMod createPayloadMod(
			@NonNull final InvoiceToExport invoice,
			@NonNull final XmlPayload xPayLoad)
	{
		final boolean reversal = invoice.isReversal();

		final boolean creditAdvise = reversal && invoice.getAmount().signum() > 0
				|| !reversal && invoice.getAmount().signum() < 0;

		return PayloadMod.builder()
				.type(RequestType.INVOICE.getValue())
				.storno(reversal)
				.creditAdvice(creditAdvise)
				.invoiceMod(createInvoiceMod(invoice))
				.bodyMod(createBodyMod(invoice, xPayLoad.getBody()))
				.build();
	}

	private InvoiceMod createInvoiceMod(@NonNull final InvoiceToExport invoice)
	{
		return InvoiceMod.builder()
				.requestTimestamp(BigInteger.valueOf(invoice.getInvoiceTimestamp().getEpochSecond()))
				.requestDate(asXmlCalendar(invoice.getInvoiceDate()))
				.requestId(invoice.getDocumentNumber())
				.build();
	}

	private XMLGregorianCalendar asXmlCalendar(@NonNull final GregorianCalendar cal)
	{
		try
		{
			return DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
		}
		catch (final DatatypeConfigurationException e)
		{
			throw new AdempiereException(e).appendParametersToMessage()
					.setParameter("cal", cal);
		}
	}

	private BodyMod createBodyMod(
			@NonNull final InvoiceToExport invoice,
			@NonNull final XmlBody xBody)
	{

		// just hand through attachments/documents that already exist within the XML that was uploaded to us
		final List<XmlDocument> documentsToExport = new ArrayList<>(xBody.getDocuments());
		documentsToExport.addAll(createDocuments(invoice.getInvoiceAttachments()));

		return BodyMod
				.builder()
				.prologMod(createPrologMod(invoice.getMetasfreshVersion()))
				.balanceMod(createBalanceMod(invoice))
				.esr(createXmlEsr(invoice.getCustomInvoicePayload()))
				.serviceModsWithSelectors(createServiceModsWithSelectors(invoice, xBody.getServices()))
				.documents(documentsToExport) // replaces possible existing documents
				.build();
	}

	private PrologMod createPrologMod(@NonNull final MetasfreshVersion metasfreshVersion)
	{
		final SoftwareMod softwareMod = createSoftwareMod(metasfreshVersion);
		return PrologMod.builder()
				.pkgMod(softwareMod)
				.generatorMod(createSoftwareMod(metasfreshVersion))
				.build();
	}

	private SoftwareMod createSoftwareMod(@NonNull final MetasfreshVersion metasfreshVersion)
	{
		final long versionNumber = metasfreshVersion.getMajor() * 100 + metasfreshVersion.getMinor(); // .. as advised in the documentation

		return SoftwareMod
				.builder()
				.name("metasfresh")
				.version(versionNumber)
				.description(metasfreshVersion.getFullVersion())
				.id(0L)
				.copyright("Copyright (C) 2018 metas GmbH")
				.build();
	}

	private BalanceMod createBalanceMod(@NonNull final InvoiceToExport invoice)
	{
		final Money amount = invoice.getAmount();

		final Money alreadyPaidAmount = invoice.getAlreadyPaidAmount();
		final BigDecimal amountDue = amount.getAmount().subtract(alreadyPaidAmount.getAmount());

		return BalanceMod.builder()
				.currency(amount.getCurrency())
				.amount(amount.getAmount())
				.amountDue(amountDue)
				.vatMod(createVatMod(invoice))
				.build();
	}

	private VatMod createVatMod(@NonNull final InvoiceToExport invoice)
	{
		final VatModBuilder builder = VatMod.builder();

		BigDecimal vatAmountSum = ZERO;
		for (final InvoiceTax invoiceTax : invoice.getInvoiceTaxes())
		{
			final XmlVatRate xVatRate = XmlVatRate.builder()
					.amount(invoiceTax.getBaseAmount())
					.vatRate(invoiceTax.getRatePercent())
					.vat(invoiceTax.getVatAmount())
					.build();
			builder.vatRate(xVatRate);

			vatAmountSum = vatAmountSum.add(invoiceTax.getVatAmount());
		}

		return builder
				.vat(vatAmountSum)
				.build();
	}

	private XmlEsr createXmlEsr(@Nullable final CustomInvoicePayload customInvoicePayload)
	{
		if (customInvoicePayload == null)
		{
			return null;
		}
		if (!(customInvoicePayload instanceof ESRPaymentInfo))
		{
			return null;
		}

		final ESRPaymentInfo paymentInfo = (ESRPaymentInfo)customInvoicePayload;
		return XmlEsr9.builder()
				.type("16or27")
				.participantNumber(paymentInfo.getParticipantNumber())
				.referenceNumber(paymentInfo.getReferenceNumber())
				.codingLine(paymentInfo.getCodingLine())
				.bank(createXmlBank(paymentInfo))
				.build();
	}

	private XmlBank createXmlBank(@NonNull final ESRPaymentInfo paymentInfo)
	{
		final XmlCompany xmlCompany = createXmlCompany(paymentInfo.getCompanyName(), paymentInfo.getAddressInfo());
		final XmlPerson xmlPerson = createXmlPerson(paymentInfo.getPersonInfo(), paymentInfo.getAddressInfo());

		if (xmlCompany == null && xmlPerson == null)
		{
			return null;
		}
		return XmlBank.builder()
				.company(xmlCompany)
				.person(xmlPerson)
				.build();
	}

	private XmlCompany createXmlCompany(
			@Nullable final String companyName,
			@Nullable final AddressInfo addressInfo)
	{
		if (companyName == null || addressInfo == null)
		{
			return null;
		}
		return XmlCompany.builder()
				.companyname(companyName)
				.postal(createXmlPostal(addressInfo))
				.build();

	}

	private XmlPerson createXmlPerson(
			@Nullable final PersonInfo personInfo,
			@Nullable final AddressInfo addressInfo)
	{
		if (personInfo == null || addressInfo == null)
		{
			return null;
		}
		return XmlPerson.builder()
				.familyname(personInfo.getFamilyName())
				.givenname(personInfo.getGivenName())
				.postal(createXmlPostal(addressInfo))
				.build();
	}

	private XmlPostal createXmlPostal(@NonNull final AddressInfo addressInfo)
	{
		return XmlPostal.builder()
				.pobox(addressInfo.getPobox())
				.street(addressInfo.getStreet())
				.zip(addressInfo.getZip())
				.city(addressInfo.getCity())
				.stateCode(addressInfo.getState())
				.countryCode(addressInfo.getIsoCountryCode())
				.build();
	}

	private List<ServiceModWithSelector> createServiceModsWithSelectors(
			@NonNull final InvoiceToExport invoice,
			@NonNull final List<XmlService> xServices)
	{
		final ImmutableList.Builder<ServiceModWithSelector> serviceMods = ImmutableList.builder();

		final ImmutableMap<Integer, XmlService> //
		recordId2xService = Maps.uniqueIndex(xServices, XmlService::getRecordId);

		for (final InvoiceLine invoiceLine : invoice.getInvoiceLines())
		{
			final String externalId = CollectionUtils.singleElement(invoiceLine.getExternalIds());
			final List<String> externalIdSegments = Splitter
					.on("_")
					.splitToList(externalId);
			Check.assume(!externalIdSegments.isEmpty(), "Every invoiceLine of an exportable invoice needs to have an externalId; invoiceLine={}, invoice={}", invoiceLine, invoice);

			final String recordIdStr = externalIdSegments.get(externalIdSegments.size() - 1);
			final int recordId = Integer.parseInt(recordIdStr);

			final XmlService xServiceForInvoiceLine = recordId2xService.get(recordId);
			final BigDecimal xServiceAmount = xServiceForInvoiceLine.getAmount();

			final Money invoiceLineMoney = invoiceLine.getLineAmount();
			final BigDecimal invoiceLineAmount = invoiceLineMoney.getAmount();

			final ServiceModWithSelectorBuilder serviceMod = ServiceModWithSelector
					.builder()
					.recordId(recordId);

			if (invoiceLineAmount.compareTo(xServiceAmount) == 0)
			{
				serviceMods.add(serviceMod.build()); // nothing to change
				continue;
			}

			// note that we don't modify the external factor
			serviceMod.amount(invoiceLineAmount);

			serviceMods.add(serviceMod.build());
		}
		return serviceMods.build();
	}

	/** attach 2ndary attachments to our XML */
	private List<XmlDocument> createDocuments(@NonNull final List<InvoiceAttachment> invoiceAttachments)
	{
		final ImmutableList.Builder<XmlDocument> xmldocuments = ImmutableList.builder();

		for (final InvoiceAttachment invoiceAttachment : invoiceAttachments)
		{
			if (invoiceAttachment.isPrimaryAttachment())
			{
				continue;
			}
			final byte[] base64Data = Base64.getEncoder().encode(invoiceAttachment.getData());
			final XmlDocument xmlDocument = XmlDocument.builder()
					.base64(base64Data)
					.filename(invoiceAttachment.getFileName())
					.mimeType(invoiceAttachment.getMimeType())
					.build();
			xmldocuments.add(xmlDocument);
		}

		return xmldocuments.build();
	}
}
