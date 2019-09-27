package de.metas.money;

import static java.math.BigDecimal.ZERO;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.adempiere.exceptions.AdempiereException;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimaps;

import de.metas.currency.Amount;
import de.metas.currency.CurrencyCode;
import de.metas.util.Check;
import de.metas.util.NumberUtils;
import de.metas.util.collections.CollectionUtils;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;

/*
 * #%L
 * de.metas.business
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

@Value
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public final class Money
{
	public static Money of(@NonNull final String value, @NonNull final CurrencyId currencyId)
	{
		return of(new BigDecimal(value), currencyId);
	}

	public static Money of(final int value, @NonNull final CurrencyId currencyId)
	{
		return of(BigDecimal.valueOf(value), currencyId);
	}

	public static Money of(@NonNull final BigDecimal value, @NonNull final CurrencyId currencyId)
	{
		return new Money(value, currencyId);
	}

	public static Money ofOrNull(@Nullable final BigDecimal value, @Nullable final CurrencyId currencyId)
	{
		if (value == null || currencyId == null)
		{
			return null;
		}
		return new Money(value, currencyId);
	}

	public static Money toZeroOrNull(@Nullable final Money money)
	{
		if (money == null)
		{
			return null;
		}
		return money.toZero();
	}

	public static Money zero(@NonNull final CurrencyId currencyId)
	{
		return new Money(ZERO, currencyId);
	}

	@JsonProperty("value")
	@Getter(AccessLevel.NONE)
	BigDecimal value;

	@JsonProperty("currencyId")
	CurrencyId currencyId;

	@Builder
	@JsonCreator
	private Money(
			@JsonProperty("value") @NonNull final BigDecimal value,
			@JsonProperty("currencyId") @NonNull final CurrencyId currencyId)
	{
		this.value = NumberUtils.stripTrailingDecimalZeros(value); // stripping trailing zeros to make sure that 4 EUR equal 4.00 EUR
		this.currencyId = currencyId;
	}

	public BigDecimal toBigDecimal()
	{
		return value;
	}

	public static BigDecimal toBigDecimalOrZero(@Nullable final Money money)
	{
		if (money == null)
		{
			return ZERO;
		}
		return money.toBigDecimal();
	}

	public int signum()
	{
		return value.signum();
	}

	public boolean isZero()
	{
		return signum() == 0;
	}

	public Money negate()
	{
		if (value.signum() == 0)
		{
			return this;
		}

		return new Money(value.negate(), currencyId);
	}

	public Money toZero()
	{
		if (isZero())
		{
			return this;
		}
		return Money.zero(currencyId);
	}

	public static CurrencyId getCommonCurrencyIdOfAll(@NonNull final Money... moneys)
	{
		Check.assumeNotEmpty(moneys, "The given moneys may not be empty");

		final Iterator<Money> moneysIterator = Stream.of(moneys)
				.filter(Predicates.notNull())
				.iterator();
		final ImmutableListMultimap<CurrencyId, Money> currency2moneys = Multimaps.index(moneysIterator, Money::getCurrencyId);
		if (currency2moneys.isEmpty())
		{
			throw new AdempiereException("The given moneys may not be empty");
		}

		final ImmutableSet<CurrencyId> currencyIds = currency2moneys.keySet();
		Check.errorIf(currencyIds.size() > 1,
				"at least two money instances have different currencies: {}", currency2moneys);

		return CollectionUtils.singleElement(currencyIds.asList());
	}

	public static boolean isSameCurrency(@NonNull final Money... moneys)
	{
		Check.assumeNotEmpty(moneys, "The given moneys may not be empty");
		return isSameCurrency(Arrays.asList(moneys));
	}

	public static boolean isSameCurrency(@NonNull final Collection<Money> moneys)
	{
		Check.assumeNotEmpty(moneys, "The given moneys may not be empty");

		final ImmutableSet<CurrencyId> currencies = moneys.stream().map(Money::getCurrencyId).collect(ImmutableSet.toImmutableSet());
		return currencies.size() == 1;
	}

	public Money add(@NonNull final Money amtToAdd)
	{
		assertCurrencyIdMatching(amtToAdd);

		if (amtToAdd.isZero())
		{
			return this;
		}
		if (isZero())
		{
			return amtToAdd;
		}

		return new Money(value.add(amtToAdd.value), currencyId);
	}

	public Money subtract(@NonNull final Money amtToSubtract)
	{
		assertCurrencyIdMatching(amtToSubtract);

		if (amtToSubtract.isZero())
		{
			return this;
		}
		return new Money(value.subtract(amtToSubtract.value), currencyId);
	}

	public Money subtract(@NonNull final BigDecimal amtToSubtract)
	{
		if (amtToSubtract.signum() == 0)
		{
			return this;
		}

		return new Money(value.subtract(amtToSubtract), currencyId);
	}

	public Money multiply(@NonNull final BigDecimal multiplicand)
	{
		if (BigDecimal.ONE.compareTo(multiplicand) == 0)
		{
			return this;
		}

		return new Money(value.multiply(multiplicand), currencyId);
	}

	public Money min(@NonNull final Money other)
	{
		assertCurrencyIdMatching(other);
		return this.value.compareTo(other.value) <= 0 ? this : other;
	}

	public Money max(@NonNull final Money other)
	{
		assertCurrencyIdMatching(other);
		return this.value.compareTo(other.value) >= 0 ? this : other;
	}

	private void assertCurrencyIdMatching(@NonNull final Money amt)
	{
		if (!Objects.equals(currencyId, amt.currencyId))
		{
			throw new AdempiereException("Amount has invalid currencyId: " + amt + ". Expected: " + currencyId);
		}
	}

	public boolean isLessThanOrEqualTo(@NonNull final Money other)
	{
		assertCurrencyIdMatching(other);
		return this.value.compareTo(other.value) <= 0;
	}

	public boolean isEqualByComparingTo(@Nullable final Money other)
	{
		if (other == null)
		{
			return false;
		}
		return other.getCurrencyId().equals(currencyId) && other.toBigDecimal().compareTo(toBigDecimal()) == 0;
	}

	public static Collector<Money, ?, Stream<Money>> sumByCurrencyAndStream()
	{
		return sumByCurrencyAnd(map -> map.values().stream());
	}

	public static Collector<Money, ?, ImmutableMap<CurrencyId, Money>> sumByCurrency()
	{
		return sumByCurrencyAnd(ImmutableMap::copyOf);
	}

	public static <T> Collector<Money, ?, T> sumByCurrencyAnd(final Function<Map<CurrencyId, Money>, T> finisher)
	{
		final Supplier<Map<CurrencyId, Money>> supplier = HashMap::new;
		final BiConsumer<Map<CurrencyId, Money>, Money> accumulator = (map, money) -> {
			map.compute(money.getCurrencyId(), (currency, moneyOld) -> moneyOld != null ? moneyOld.add(money) : money);
		};
		final BinaryOperator<Map<CurrencyId, Money>> combiner = (l, r) -> {
			r.values().forEach(money -> accumulator.accept(l, money));
			return l;
		};

		return Collector.of(supplier, accumulator, combiner, finisher);
	}

	public Amount toAmount(@NonNull final Function<CurrencyId, CurrencyCode> currencyCodeMapper)
	{
		return Amount.of(toBigDecimal(), currencyCodeMapper.apply(getCurrencyId()));
	}

}
