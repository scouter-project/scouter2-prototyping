package scouter2.common.util;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-08
 */
public class MiscUtilTest {

    @Test
    public void camelCaseToUnderscore() {

        assertThat(MiscUtil.camelCaseToUnderscore("myAuctionTeam")).isEqualTo("my_auction_team");
        assertThat(MiscUtil.camelCaseToUnderscore("_myAuctionTeam")).isEqualTo("_my_auction_team");
        assertThat(MiscUtil.camelCaseToUnderscore("__myAuctionTeam")).isEqualTo("__my_auction_team");

    }

    @Test
    public void underscoreToCamelCase() {

        assertThat(MiscUtil.underscoreToCamelCase("my_auction_team")).isEqualTo("myAuctionTeam");
        assertThat(MiscUtil.underscoreToCamelCase("_my_auction_team")).isEqualTo("_myAuctionTeam");
        assertThat(MiscUtil.underscoreToCamelCase("__my_auction_team")).isEqualTo("__myAuctionTeam");

    }
}