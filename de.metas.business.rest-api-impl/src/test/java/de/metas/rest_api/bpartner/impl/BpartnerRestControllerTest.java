package de.metas.rest_api.bpartner.impl;

import static de.metas.rest_api.bpartner.impl.BPartnerRecordsUtil.AD_ORG_ID;
import static de.metas.rest_api.bpartner.impl.BPartnerRecordsUtil.AD_USER_ID;
import static de.metas.rest_api.bpartner.impl.BPartnerRecordsUtil.BP_GROUP_RECORD_NAME;
import static de.metas.rest_api.bpartner.impl.BPartnerRecordsUtil.C_BBPARTNER_LOCATION_ID;
import static de.metas.rest_api.bpartner.impl.BPartnerRecordsUtil.C_BPARTNER_EXTERNAL_ID;
import static de.metas.rest_api.bpartner.impl.BPartnerRecordsUtil.C_BPARTNER_ID;
import static de.metas.rest_api.bpartner.impl.BPartnerRecordsUtil.C_BPARTNER_LOCATION_GLN;
import static de.metas.rest_api.bpartner.impl.BPartnerRecordsUtil.C_BP_GROUP_ID;
import static de.metas.rest_api.bpartner.impl.BPartnerRecordsUtil.createBPartnerData;
import static io.github.jsonSnapshot.SnapshotMatcher.expect;
import static io.github.jsonSnapshot.SnapshotMatcher.start;
import static io.github.jsonSnapshot.SnapshotMatcher.validateSnapshots;
import static org.adempiere.model.InterfaceWrapperHelper.load;
import static org.adempiere.model.InterfaceWrapperHelper.newInstance;
import static org.adempiere.model.InterfaceWrapperHelper.refresh;
import static org.adempiere.model.InterfaceWrapperHelper.saveRecord;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.InputStream;
import java.util.Optional;

import org.adempiere.ad.table.RecordChangeLogRepository;
import org.adempiere.ad.wrapper.POJOLookupMap;
import org.adempiere.test.AdempiereTestHelper;
import org.compiere.model.I_AD_SysConfig;
import org.compiere.model.I_AD_User;
import org.compiere.model.I_C_BP_Group;
import org.compiere.model.I_C_BPartner;
import org.compiere.model.I_C_BPartner_Location;
import org.compiere.model.I_C_Country;
import org.compiere.model.I_C_Location;
import org.compiere.util.Env;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.google.common.collect.ImmutableList;

import de.metas.bpartner.BPGroupRepository;
import de.metas.bpartner.BPartnerContactId;
import de.metas.bpartner.BPartnerId;
import de.metas.bpartner.BPartnerLocationId;
import de.metas.bpartner.composite.BPartnerComposite;
import de.metas.bpartner.composite.BPartnerCompositeAndContactId;
import de.metas.bpartner.composite.BPartnerLocation;
import de.metas.bpartner.composite.repository.BPartnerCompositeRepository;
import de.metas.bpartner.service.BPartnerContactQuery;
import de.metas.bpartner.service.BPartnerQuery;
import de.metas.bpartner.service.IBPartnerBL;
import de.metas.bpartner.service.impl.BPartnerBL;
import de.metas.greeting.GreetingRepository;
import de.metas.rest_api.JsonExternalId;
import de.metas.rest_api.MetasfreshId;
import de.metas.rest_api.SyncAdvise;
import de.metas.rest_api.SyncAdvise.IfExists;
import de.metas.rest_api.SyncAdvise.IfNotExists;
import de.metas.rest_api.bpartner.impl.bpartnercomposite.JsonServiceFactory;
import de.metas.rest_api.bpartner.request.JsonRequestBPartner;
import de.metas.rest_api.bpartner.request.JsonRequestBPartnerUpsert;
import de.metas.rest_api.bpartner.request.JsonRequestBPartnerUpsertItem;
import de.metas.rest_api.bpartner.request.JsonRequestComposite;
import de.metas.rest_api.bpartner.request.JsonRequestContactUpsert;
import de.metas.rest_api.bpartner.request.JsonRequestContactUpsertItem;
import de.metas.rest_api.bpartner.request.JsonRequestLocationUpsert;
import de.metas.rest_api.bpartner.request.JsonRequestLocationUpsertItem;
import de.metas.rest_api.bpartner.request.JsonResponseUpsert;
import de.metas.rest_api.bpartner.request.JsonResponseUpsertItem;
import de.metas.rest_api.bpartner.response.JsonResponseComposite;
import de.metas.rest_api.bpartner.response.JsonResponseCompositeList;
import de.metas.rest_api.bpartner.response.JsonResponseContact;
import de.metas.rest_api.bpartner.response.JsonResponseLocation;
import de.metas.rest_api.utils.MissingResourceException;
import de.metas.user.UserId;
import de.metas.user.UserRepository;
import de.metas.util.JSONObjectMapper;
import de.metas.util.Services;
import de.metas.util.lang.UIDStringUtil;
import de.metas.util.rest.ExternalId;
import de.metas.util.time.SystemTime;
import lombok.NonNull;
import lombok.Value;

/*
 * #%L
 * de.metas.business.rest-api-impl
 * %%
 * Copyright (C) 2019 metas GmbH
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

class BpartnerRestControllerTest
{
	private BpartnerRestController bpartnerRestController;

	private BPartnerCompositeRepository bpartnerCompositeRepository;

	@BeforeAll
	static void initStatic()
	{
		start(AdempiereTestHelper.SNAPSHOT_CONFIG);
	}

	@AfterAll
	static void afterAll()
	{
		validateSnapshots();
	}

	@BeforeEach
	void init()
	{
		AdempiereTestHelper.get().init();

		Services.registerService(IBPartnerBL.class, new BPartnerBL(new UserRepository()));

		bpartnerCompositeRepository = new BPartnerCompositeRepository(new MockLogEntriesRepository());
		final JsonServiceFactory jsonServiceFactory = new JsonServiceFactory(
				bpartnerCompositeRepository,
				new BPGroupRepository(),
				new GreetingRepository(),
				new RecordChangeLogRepository());

		bpartnerRestController = new BpartnerRestController(new BPartnerEndpointService(jsonServiceFactory), jsonServiceFactory);

		final I_C_BP_Group bpGroupRecord = newInstance(I_C_BP_Group.class);
		bpGroupRecord.setC_BP_Group_ID(C_BP_GROUP_ID);
		bpGroupRecord.setName(BP_GROUP_RECORD_NAME);
		saveRecord(bpGroupRecord);

		createBPartnerData(0);

		Env.setContext(Env.getCtx(), Env.CTXNAME_AD_Org_ID, AD_ORG_ID);
	}

	@Test
	void retrieveBPartner_ext()
	{
		// invoke the method under test
		final ResponseEntity<JsonResponseComposite> result = bpartnerRestController.retrieveBPartner("ext-" + C_BPARTNER_EXTERNAL_ID);

		assertThat(result.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
		final JsonResponseComposite resultBody = result.getBody();

		expect(resultBody).toMatchSnapshot();
	}

	@Test
	void retrieveBPartner_id()
	{
		// invoke the method under test
		final ResponseEntity<JsonResponseComposite> result = bpartnerRestController.retrieveBPartner(Integer.toString(C_BPARTNER_ID));

		assertThat(result.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
		final JsonResponseComposite resultBody = result.getBody();

		expect(resultBody).toMatchSnapshot();
	}

	@Test
	void retrieveBPartner_gln()
	{
		// invoke the method under test
		final ResponseEntity<JsonResponseComposite> result = bpartnerRestController.retrieveBPartner("gln-" + C_BPARTNER_LOCATION_GLN);

		assertThat(result.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
		final JsonResponseComposite resultBody = result.getBody();

		expect(resultBody).toMatchSnapshot();
	}

	@Test
	void retrieveBPartnerContact()
	{
		// invoke the method under test
		final ResponseEntity<JsonResponseContact> result = bpartnerRestController.retrieveBPartnerContact(
				"ext-" + C_BPARTNER_EXTERNAL_ID,
				Integer.toString(AD_USER_ID));

		assertThat(result.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
		final JsonResponseContact resultBody = result.getBody();

		expect(resultBody).toMatchSnapshot();
	}

	@Test
	void retrieveBPartnerLocation()
	{
		// invoke the method under test
		final ResponseEntity<JsonResponseLocation> result = bpartnerRestController.retrieveBPartnerLocation(
				"ext-" + C_BPARTNER_EXTERNAL_ID,
				"gln-" + C_BPARTNER_LOCATION_GLN);

		assertThat(result.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
		final JsonResponseLocation resultBody = result.getBody();

		expect(resultBody).toMatchSnapshot();
	}

	@Test
	void createOrUpdateBPartner_create()
	{
		final int initialBPartnerRecordCount = POJOLookupMap.get().getRecords(I_C_BPartner.class).size();
		final int initialUserRecordCount = POJOLookupMap.get().getRecords(I_AD_User.class).size();
		final int initialBPartnerLocationRecordCount = POJOLookupMap.get().getRecords(I_C_BPartner_Location.class).size();
		final int initialLocationRecordCount = POJOLookupMap.get().getRecords(I_C_Location.class).size();

		createCountryRecord("CH");
		createCountryRecord("DE");

		final String externalId = C_BPARTNER_EXTERNAL_ID + "_2";
		final JsonRequestComposite bpartnerComposite = MockedDataUtil
				.createMockBPartner("ext-" + externalId);

		assertThat(bpartnerComposite.getContactsNotNull().getRequestItems()).hasSize(2); // guard
		assertThat(bpartnerComposite.getLocationsNotNull().getRequestItems()).hasSize(2);// guard

		final JsonRequestBPartner bpartner = bpartnerComposite.getBpartner()
				.toBuilder()
				.group(BP_GROUP_RECORD_NAME)
				.build();

		final JsonRequestBPartnerUpsertItem requestItem = JsonRequestBPartnerUpsertItem.builder()
				.bpartnerIdentifier("ext-" + externalId)
				.bpartnerComposite(bpartnerComposite.toBuilder()
						.bpartner(bpartner)
						.build())
				.build();

		final JsonRequestBPartnerUpsert bpartnerUpsertRequest = JsonRequestBPartnerUpsert.builder()
				.syncAdvise(SyncAdvise.CREATE_OR_MERGE)
				.requestItem(requestItem)
				.build();

		SystemTime.setTimeSource(() -> 1561134560); // Fri, 21 Jun 2019 16:29:20 GMT

		// JSONObjectMapper.forClass(JsonRequestBPartnerUpsert.class).writeValueAsString(bpartnerUpsertRequest);
		// invoke the method under test
		final ResponseEntity<JsonResponseUpsert> result = bpartnerRestController.createOrUpdateBPartner(bpartnerUpsertRequest);

		final MetasfreshId metasfreshId = assertUpsertResultOK(result, "ext-" + externalId);
		BPartnerId bpartnerId = BPartnerId.ofRepoId(metasfreshId.getValue());

		final BPartnerComposite persistedResult = bpartnerCompositeRepository.getById(bpartnerId);
		expect(persistedResult).toMatchSnapshot();

		assertThat(POJOLookupMap.get().getRecords(I_C_BPartner.class)).hasSize(initialBPartnerRecordCount + 1);
		assertThat(POJOLookupMap.get().getRecords(I_AD_User.class)).hasSize(initialUserRecordCount + 2);
		assertThat(POJOLookupMap.get().getRecords(I_C_BPartner_Location.class)).hasSize(initialBPartnerLocationRecordCount + 2);
		assertThat(POJOLookupMap.get().getRecords(I_C_Location.class)).hasSize(initialLocationRecordCount + 2);
	}

	@Test
	void createOrUpdateBPartner_update_builder()
	{
		final JsonRequestBPartnerUpsert bpartnerUpsertRequest = JsonRequestBPartnerUpsert.builder()
				.syncAdvise(SyncAdvise.builder()
						.ifExists(IfExists.UPDATE_MERGE)
						.ifNotExists(IfNotExists.CREATE)
						.build())
				.requestItem(JsonRequestBPartnerUpsertItem.builder()
						.bpartnerIdentifier("ext-1234567")
						.bpartnerComposite(JsonRequestComposite.builder()
								.bpartner(JsonRequestBPartner.builder()
										.companyName("otherCompanyName")
										.externalId(JsonExternalId.of("1234567otherExternalId"))
										.code("other12345")
										.build())
								.build())
						.build())
				.build();

		createOrUpdateBPartner_update_performTest(bpartnerUpsertRequest);
	}

	/**
	 * Like {@link #createOrUpdateBPartner_update_builder()}, but get the rest file from a JSON string
	 */
	@Test
	void createOrUpdateBPartner_update_json()
	{
		final JsonRequestBPartnerUpsert bpartnerUpsertRequest = loadUpsertRequest("BpartnerRestControllerTest_Simple_BPartner.json");
		createOrUpdateBPartner_update_performTest(bpartnerUpsertRequest);
	}

	/**
	 * Verifies a small&simple JSON that identifies a BPartner by its value and then updates that value.
	 */
	@Test
	void createOrUpdateBPartner_update_C_BPartner_Value_OK()
	{
		final JsonRequestBPartnerUpsert bpartnerUpsertRequest = loadUpsertRequest("BpartnerRestControllerTest_update_C_BPartner_Value.json");

		final I_C_BPartner bpartnerRecord = newInstance(I_C_BPartner.class);
		bpartnerRecord.setAD_Org_ID(AD_ORG_ID);
		bpartnerRecord.setName("bpartnerRecord.name");
		bpartnerRecord.setValue("12345");
		bpartnerRecord.setCompanyName("bpartnerRecord.companyName");
		bpartnerRecord.setC_BP_Group_ID(C_BP_GROUP_ID);
		saveRecord(bpartnerRecord);

		final RecordCounts inititalCounts = new RecordCounts();

		// invoke the method under test
		bpartnerRestController.createOrUpdateBPartner(bpartnerUpsertRequest);

		inititalCounts.assertCountsUnchanged();

		// verify that the bpartner-record was updated
		refresh(bpartnerRecord);
		assertThat(bpartnerRecord.getValue()).isEqualTo("12345_updated");
	}

	/**
	 * Update a bpartner and insert a location at the same time.
	 */
	@Test
	void createOrUpdateBPartner_Update_BPartner_Name_Insert_Location()
	{
		SystemTime.setTimeSource(() -> 1563553074); // Fri, 19 Jul 2019 16:17:54 GMT

		final JsonRequestBPartnerUpsert bpartnerUpsertRequest = loadUpsertRequest("BPartnerRestControllerTest_Update_BPartner_Name_Insert_Location.json");

		assertThat(bpartnerUpsertRequest.getRequestItems()).hasSize(1);
		assertThat(bpartnerUpsertRequest.getRequestItems().get(0).getBpartnerComposite().getLocationsNotNull().getRequestItems()).hasSize(1);

		final I_C_BPartner bpartnerRecord = newInstance(I_C_BPartner.class);
		bpartnerRecord.setAD_Org_ID(AD_ORG_ID);
		bpartnerRecord.setName("bpartnerRecord.name");
		bpartnerRecord.setValue("12345");
		bpartnerRecord.setCompanyName("bpartnerRecord.companyName");
		bpartnerRecord.setC_BP_Group_ID(C_BP_GROUP_ID);
		saveRecord(bpartnerRecord);

		final RecordCounts inititalCounts = new RecordCounts();

		// invoke the method under test
		bpartnerRestController.createOrUpdateBPartner(bpartnerUpsertRequest);

		inititalCounts.assertBPartnerCountChangedBy(0);
		inititalCounts.assertUserCountChangedBy(0);
		inititalCounts.assertLocationCountChangedBy(1);

		// verify that the bpartner-record was updated
		refresh(bpartnerRecord);
		assertThat(bpartnerRecord.getValue()).isEqualTo("12345"); // shall be unchanged
		assertThat(bpartnerRecord.getName()).isEqualTo("bpartnerRecord.name_updated");

		// use the rest controller to get the json that we can then verify
		final ResponseEntity<JsonResponseComposite> result = bpartnerRestController.retrieveBPartner("val-12345");
		assertThat(result.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
		final JsonResponseComposite resultBody = result.getBody();
		expect(resultBody).toMatchSnapshot();

		// finally, also make sure that if we repeat the same invocation, no new location record is created
		final RecordCounts countsBefore2ndInvocation = new RecordCounts();
		bpartnerRestController.createOrUpdateBPartner(bpartnerUpsertRequest);
		countsBefore2ndInvocation.assertCountsUnchanged();
	}

	@Test
	void createOrUpdateBPartner_update_C_BPartner_Value_NoSuchPartner()
	{
		final JsonRequestBPartnerUpsert bpartnerUpsertRequest = loadUpsertRequest("BpartnerRestControllerTest_update_C_BPartner_Value.json");

		final I_C_BPartner bpartnerRecord = newInstance(I_C_BPartner.class);
		bpartnerRecord.setAD_Org_ID(AD_ORG_ID);
		bpartnerRecord.setName("bpartnerRecord.name");
		bpartnerRecord.setValue("12345nosuchvalue");
		bpartnerRecord.setCompanyName("bpartnerRecord.companyName");
		bpartnerRecord.setC_BP_Group_ID(C_BP_GROUP_ID);
		saveRecord(bpartnerRecord);

		// invoke the method under test
		assertThatThrownBy(() -> bpartnerRestController.createOrUpdateBPartner(bpartnerUpsertRequest))
				.isInstanceOf(MissingResourceException.class)
				.hasMessageContaining("bpartner");
	}

	/**
	 * Verifies that if an upsert request contains two locations pointing to the same "real" location, then the are applied one after another.
	 */
	@Test
	void createOrUpdateBPartner_duplicate_location()
	{
		final JsonRequestBPartnerUpsert bpartnerUpsertRequest = loadUpsertRequest("BpartnerRestControllerTest_duplicate_location.json");

		createBPartnerData(0);

		// invoke the method under test
		bpartnerRestController.createOrUpdateBPartner(bpartnerUpsertRequest);

		final I_C_BPartner_Location bpartnerLocationRecord = load(C_BBPARTNER_LOCATION_ID, I_C_BPartner_Location.class);
		assertThat(bpartnerLocationRecord.isActive()).isFalse(); // from the second JSONLocation

		final I_C_Location locationRecord = bpartnerLocationRecord.getC_Location();
		assertThat(locationRecord.getAddress1()).isEqualTo("address1_metasfreshId"); // from the first JSONLocation
		assertThat(locationRecord.getAddress2()).isEqualTo("address2_extId"); // from the second JSONLocation
	}

	private JsonRequestBPartnerUpsert loadUpsertRequest(final String jsonFileName)
	{
		final InputStream stream = getClass().getClassLoader().getResourceAsStream("de/metas/rest_api/bpartner/impl/" + jsonFileName);
		assertThat(stream).isNotNull();
		final JsonRequestBPartnerUpsert bpartnerUpsertRequest = JSONObjectMapper.forClass(JsonRequestBPartnerUpsert.class).readValue(stream);
		return bpartnerUpsertRequest;
	}

	private MetasfreshId assertUpsertResultOK(
			@NonNull final ResponseEntity<JsonResponseUpsert> result,
			@NonNull final String bpartnerIdentifier)
	{
		assertThat(result.getStatusCode()).isEqualByComparingTo(HttpStatus.CREATED);

		final JsonResponseUpsert resultBody = result.getBody();
		assertThat(resultBody.getResponseItems()).hasSize(1);

		final JsonResponseUpsertItem responseItem = resultBody.getResponseItems().get(0);
		assertThat(responseItem.getIdentifier()).isEqualTo(bpartnerIdentifier);

		final MetasfreshId metasfreshId = responseItem.getMetasfreshId();
		return metasfreshId;
	}

	private void createOrUpdateBPartner_update_performTest(@NonNull final JsonRequestBPartnerUpsert bpartnerUpsertRequest)
	{
		final I_C_BPartner bpartnerRecord = newInstance(I_C_BPartner.class);
		bpartnerRecord.setAD_Org_ID(AD_ORG_ID);
		bpartnerRecord.setExternalId("1234567");
		bpartnerRecord.setName("bpartnerRecord.name");
		bpartnerRecord.setValue("bpartnerRecord.value");
		bpartnerRecord.setCompanyName("bpartnerRecord.companyName");
		bpartnerRecord.setC_BP_Group_ID(C_BP_GROUP_ID);
		saveRecord(bpartnerRecord);

		final RecordCounts inititalCounts = new RecordCounts();

		// invoke the method under test
		final ResponseEntity<JsonResponseUpsert> result = bpartnerRestController.createOrUpdateBPartner(bpartnerUpsertRequest);

		inititalCounts.assertCountsUnchanged();

		assertThat(result.getBody().getResponseItems()).hasSize(1);
		final JsonResponseUpsertItem jsonResponseUpsertItem = result.getBody().getResponseItems().get(0);

		assertThat(jsonResponseUpsertItem.getIdentifier()).isEqualTo("ext-1234567");
		assertThat(jsonResponseUpsertItem.getMetasfreshId().getValue()).isEqualTo(bpartnerRecord.getC_BPartner_ID());

		// verify that the bpartner-record was updated
		refresh(bpartnerRecord);
		assertThat(bpartnerRecord.getCompanyName()).isEqualTo("otherCompanyName");
		assertThat(bpartnerRecord.getValue()).isEqualTo("other12345");
	}

	@Value
	private static class RecordCounts
	{
		int initialBPartnerRecordCount;
		int initialUserRecordCount;
		int initialBPartnerLocationRecordCount;
		int initialLocationRecordCount;

		private RecordCounts()
		{
			initialBPartnerRecordCount = POJOLookupMap.get().getRecords(I_C_BPartner.class).size();
			initialUserRecordCount = POJOLookupMap.get().getRecords(I_AD_User.class).size();
			initialBPartnerLocationRecordCount = POJOLookupMap.get().getRecords(I_C_BPartner_Location.class).size();
			initialLocationRecordCount = POJOLookupMap.get().getRecords(I_C_Location.class).size();
		}

		private void assertCountsUnchanged()
		{
			assertBPartnerCountChangedBy(0);
			assertUserCountChangedBy(0);
			assertLocationCountChangedBy(0);
		}

		private void assertUserCountChangedBy(final int offset)
		{
			assertThat(POJOLookupMap.get().getRecords(I_AD_User.class)).hasSize(initialUserRecordCount + offset);
		}

		private void assertLocationCountChangedBy(final int offset)
		{
			assertThat(POJOLookupMap.get().getRecords(I_C_BPartner_Location.class)).hasSize(initialBPartnerLocationRecordCount + offset);
			assertThat(POJOLookupMap.get().getRecords(I_C_Location.class)).hasSize(initialLocationRecordCount + offset);
		}

		private void assertBPartnerCountChangedBy(final int offset)
		{
			assertThat(POJOLookupMap.get().getRecords(I_C_BPartner.class)).hasSize(initialBPartnerRecordCount + offset);
		}
	}

	private void createCountryRecord(@NonNull final String countryCode)
	{
		final I_C_Country deCountryRecord = newInstance(I_C_Country.class);
		deCountryRecord.setCountryCode(countryCode);
		saveRecord(deCountryRecord);
	}

	@Test
	void createOrUpdateContact_create()
	{
		final JsonRequestContactUpsertItem jsonContact = MockedDataUtil.createMockContact("newContact-");

		SystemTime.setTimeSource(() -> 1561134560); // Fri, 21 Jun 2019 16:29:20 GMT

		// invoke the method under test
		final ResponseEntity<JsonResponseUpsert> result = bpartnerRestController.createOrUpdateContact(
				"gln-" + C_BPARTNER_LOCATION_GLN,
				JsonRequestContactUpsert.builder().requestItem(jsonContact).build());

		assertThat(result.getStatusCode()).isEqualByComparingTo(HttpStatus.CREATED);

		final JsonResponseUpsert response = result.getBody();
		assertThat(response.getResponseItems()).hasSize(1);
		final JsonResponseUpsertItem responseItem = response.getResponseItems().get(0);
		assertThat(responseItem.getIdentifier()).isEqualTo(jsonContact.getContactIdentifier());

		final MetasfreshId metasfreshId = responseItem.getMetasfreshId();

		final BPartnerContactQuery bpartnerContactQuery = BPartnerContactQuery.builder().userId(UserId.ofRepoId(metasfreshId.getValue())).build();
		final Optional<BPartnerCompositeAndContactId> optContactIdAndBPartner = bpartnerCompositeRepository.getByContact(bpartnerContactQuery);
		assertThat(optContactIdAndBPartner).isPresent();

		final BPartnerContactId resultContactId = optContactIdAndBPartner.get().getBpartnerContactId();
		assertThat(resultContactId.getRepoId()).isEqualTo(metasfreshId.getValue());

		final BPartnerComposite persistedResult = optContactIdAndBPartner.get().getBpartnerComposite();
		expect(persistedResult).toMatchSnapshot();
	}

	@Test
	void createOrUpdateLocation_create()
	{
		createCountryRecord("DE");
		final JsonRequestLocationUpsertItem jsonLocation = MockedDataUtil.createMockLocation("newLocation-", "DE");

		SystemTime.setTimeSource(() -> 1561134560); // Fri, 21 Jun 2019 16:29:20 GMT

		// invoke the method under test
		final ResponseEntity<JsonResponseUpsert> result = bpartnerRestController.createOrUpdateLocation(
				"ext-" + C_BPARTNER_EXTERNAL_ID,
				JsonRequestLocationUpsert.builder().requestItem(jsonLocation).build());

		assertThat(result.getStatusCode()).isEqualByComparingTo(HttpStatus.CREATED);

		final JsonResponseUpsert response = result.getBody();
		assertThat(response.getResponseItems()).hasSize(1);
		final JsonResponseUpsertItem responseItem = response.getResponseItems().get(0);

		assertThat(responseItem.getIdentifier()).isEqualTo(jsonLocation.getLocationIdentifier());

		final MetasfreshId metasfreshId = responseItem.getMetasfreshId();

		final BPartnerQuery query = BPartnerQuery.builder().externalId(ExternalId.of(C_BPARTNER_EXTERNAL_ID)).build();
		final ImmutableList<BPartnerComposite> persistedPage = bpartnerCompositeRepository.getByQuery(query);

		assertThat(persistedPage).hasSize(1);

		final BPartnerComposite persistedResult = persistedPage.get(0);
		final Optional<BPartnerLocation> persistedLocation = persistedResult.extractLocation(BPartnerLocationId.ofRepoId(persistedResult.getBpartner().getId(), metasfreshId.getValue()));
		assertThat(persistedLocation).isPresent();

		assertThat(persistedLocation.get().getId().getRepoId()).isEqualTo(metasfreshId.getValue());

		expect(persistedResult).toMatchSnapshot();
	}

	@Test
	void retrieveBPartnersSince()
	{

		final I_AD_SysConfig sysConfigRecord = newInstance(I_AD_SysConfig.class);
		sysConfigRecord.setName(BPartnerEndpointService.SYSCFG_BPARTNER_PAGE_SIZE);
		sysConfigRecord.setValue("2");
		saveRecord(sysConfigRecord);

		createBPartnerData(1);
		createBPartnerData(2);
		createBPartnerData(3);
		createBPartnerData(4);

		SystemTime.setTimeSource(() -> 1561014385); // Thu, 20 Jun 2019 07:06:25 GMT
		UIDStringUtil.setRandomUUIDSource(() -> "e57d6ba2-e91e-4557-8fc7-cb3c0acfe1f1");

		// invoke the method under test
		final ResponseEntity<JsonResponseCompositeList> page1 = bpartnerRestController.retrieveBPartnersSince(0L, null);

		assertThat(page1.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
		final JsonResponseCompositeList page1Body = page1.getBody();
		assertThat(page1Body.getItems()).hasSize(2);

		final String page2Id = page1Body.getPagingDescriptor().getNextPage();
		assertThat(page2Id).isNotEmpty();

		final ResponseEntity<JsonResponseCompositeList> page2 = bpartnerRestController.retrieveBPartnersSince(null, page2Id);

		assertThat(page2.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
		final JsonResponseCompositeList page2Body = page2.getBody();
		assertThat(page2Body.getItems()).hasSize(2);

		final String page3Id = page2Body.getPagingDescriptor().getNextPage();
		assertThat(page3Id).isNotEmpty();

		final ResponseEntity<JsonResponseCompositeList> page3 = bpartnerRestController.retrieveBPartnersSince(null, page3Id);

		assertThat(page3.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
		final JsonResponseCompositeList page3Body = page3.getBody();
		assertThat(page3Body.getItems()).hasSize(1);

		assertThat(page3Body.getPagingDescriptor().getNextPage()).isNull();

		expect(page1Body, page2Body, page3Body).toMatchSnapshot();
	}
}
