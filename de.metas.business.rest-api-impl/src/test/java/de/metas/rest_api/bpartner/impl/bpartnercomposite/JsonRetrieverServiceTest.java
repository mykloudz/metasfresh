package de.metas.rest_api.bpartner.impl.bpartnercomposite;

import static de.metas.rest_api.bpartner.impl.BPartnerRecordsUtil.AD_ORG_ID;
import static de.metas.rest_api.bpartner.impl.BPartnerRecordsUtil.BP_GROUP_RECORD_NAME;
import static de.metas.rest_api.bpartner.impl.BPartnerRecordsUtil.*;
import static de.metas.rest_api.bpartner.impl.BPartnerRecordsUtil.createBPartnerData;
import static io.github.jsonSnapshot.SnapshotMatcher.expect;
import static io.github.jsonSnapshot.SnapshotMatcher.start;
import static io.github.jsonSnapshot.SnapshotMatcher.validateSnapshots;
import static org.adempiere.model.InterfaceWrapperHelper.newInstance;
import static org.adempiere.model.InterfaceWrapperHelper.saveRecord;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.adempiere.ad.table.RecordChangeLogRepository;
import org.adempiere.test.AdempiereTestHelper;
import org.compiere.model.I_C_BP_Group;
import org.compiere.util.Env;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;

import de.metas.bpartner.BPGroupRepository;
import de.metas.bpartner.composite.BPartnerComposite;
import de.metas.bpartner.composite.repository.BPartnerCompositeRepository;
import de.metas.greeting.GreetingRepository;
import de.metas.rest_api.JsonExternalId;
import de.metas.rest_api.bpartner.impl.MockLogEntriesRepository;

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

class JsonRetrieverServiceTest
{
	private JsonRetrieverService jsonRetrieverService;

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

		final BPartnerCompositeRepository bpartnerCompositeRepository = new BPartnerCompositeRepository(new MockLogEntriesRepository());

		final JsonServiceFactory jsonServiceFactory = new JsonServiceFactory(
				bpartnerCompositeRepository,
				new BPGroupRepository(),
				new GreetingRepository(),
				new RecordChangeLogRepository());

		jsonRetrieverService = jsonServiceFactory.createRetriever();

		final I_C_BP_Group bpGroupRecord = newInstance(I_C_BP_Group.class);
		bpGroupRecord.setC_BP_Group_ID(C_BP_GROUP_ID);
		bpGroupRecord.setName(BP_GROUP_RECORD_NAME);
		saveRecord(bpGroupRecord);

		createBPartnerData(0);

		Env.setContext(Env.getCtx(), Env.CTXNAME_AD_Org_ID, AD_ORG_ID);
	}

	@Test
	void retrieveBPartnerComposite_byBPartnerCompositeLookupKeys()
	{
		final ImmutableList<BPartnerCompositeLookupKey> bpartnerLookupKeys = ImmutableList
				.of(
						BPartnerCompositeLookupKey.ofJsonExternalId(JsonExternalId.of(C_BPARTNER_EXTERNAL_ID)),
						BPartnerCompositeLookupKey.ofCode(C_BPARTNER_VALUE));

		final Optional<BPartnerComposite> result = jsonRetrieverService.getBPartnerComposite(bpartnerLookupKeys);

		assertThat(result).isNotEmpty();
		expect(result.get()).toMatchSnapshot();
	}

	/** verifies that if a bpartner is found&loaded by one lookup property, it is also cached to be found by the other properties */
	@Test
	void retrieveBPartnerComposite_retrieveBPartnerCompositeAssertCacheHit()
	{

		final ImmutableList<BPartnerCompositeLookupKey> bpartnerLookupKey = ImmutableList.of(BPartnerCompositeLookupKey.ofCode(C_BPARTNER_VALUE));
		final Optional<BPartnerComposite> result = jsonRetrieverService.getBPartnerComposite(bpartnerLookupKey);

		final ImmutableList<BPartnerCompositeLookupKey> bpartnerLookupKey2 = ImmutableList.of(BPartnerCompositeLookupKey.ofJsonExternalId(JsonExternalId.of(C_BPARTNER_EXTERNAL_ID)));
		final Optional<BPartnerComposite> result2 = jsonRetrieverService.getBPartnerCompositeAssertCacheHit(bpartnerLookupKey2);

		assertThat(result2).isNotSameAs(result); // not sameAs, because the composite is mutable and the retriever shall give us own own instance each time.
		assertThat(result2).isEqualTo(result);
	}

}
