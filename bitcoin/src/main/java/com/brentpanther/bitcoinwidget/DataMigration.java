package com.brentpanther.bitcoinwidget;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import static com.brentpanther.bitcoinwidget.Coin.BCH;
import static com.brentpanther.bitcoinwidget.Coin.BTC;
import static com.brentpanther.bitcoinwidget.Coin.DASH;
import static com.brentpanther.bitcoinwidget.Coin.IOTA;
import static com.brentpanther.bitcoinwidget.Coin.LTC;
import static com.brentpanther.bitcoinwidget.Currency.TRY;
import static com.brentpanther.bitcoinwidget.Currency.USD;


/**
 * One-off migrations so that existing users' widgets do not get affected by data changes.
 */
class DataMigration {

    private static final String EXCHANGE_OVERRIDE_MIGRATION = "exchange_override";
    private static final String QUOINE_MIGRATION = "quoine";
    private static final String COINMARKETCAP_MIGRATION = "coinmarketcap";
    private static final String BITTREX_TO_BCH = "bittrex_to_bch";
    private static final String GDAX = "gdax_to_coinbasepro";

    static void migrate(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean hasOverrideMigration = prefs.getBoolean(EXCHANGE_OVERRIDE_MIGRATION, false);
        if (!hasOverrideMigration) {
            migrateExchangeCoinAndCurrencyNames();
            prefs.edit().putBoolean(EXCHANGE_OVERRIDE_MIGRATION, true).apply();
        }
        boolean hasQuoineMigration = prefs.getBoolean(QUOINE_MIGRATION, false);
        if (!hasQuoineMigration) {
            migrateQuoine();
            prefs.edit().putBoolean(QUOINE_MIGRATION, true).apply();
        }
        boolean hasCoinMarketCapMigration = prefs.getBoolean(COINMARKETCAP_MIGRATION, false);
        if (!hasCoinMarketCapMigration) {
            migrateCoinMarketCapV2();
            prefs.edit().putBoolean(COINMARKETCAP_MIGRATION, true).apply();
        }
        boolean hasBittrexBCHMigration = prefs.getBoolean(BITTREX_TO_BCH, false);
        if (!hasBittrexBCHMigration) {
            migrateBittrexBCH();
            prefs.edit().putBoolean(BITTREX_TO_BCH, true).apply();
        }
        boolean hasGDAXMigration = prefs.getBoolean(GDAX, false);
        if (!hasGDAXMigration) {
            migrateGDAX();
            prefs.edit().putBoolean(GDAX, true).apply();
        }
    }

    private static void migrateGDAX() {
        int[] widgetIds = WidgetApplication.getInstance().getWidgetIds();
        for (int widgetId : widgetIds) {
            Prefs prefs = new Prefs(widgetId);
            String exchange = prefs.getValue("exchange");
            if ("GDAX".equals(exchange)) {
                prefs.setValue("exchange", "COINBASEPRO");
            }
        }
    }

    // bittrex changed from using BCC to BCH
    private static void migrateBittrexBCH() {
        int[] widgetIds = WidgetApplication.getInstance().getWidgetIds();
        for (int widgetId : widgetIds) {
            Prefs prefs = new Prefs(widgetId);
            String exchange = prefs.getValue("exchange");
            if ("BITTREX".equals(exchange)) {
                Coin coin = prefs.getCoin();
                String exchangeCoinName = prefs.getExchangeCoinName();
                if ("BCC".equals(exchangeCoinName)) {
                    prefs.setValue("coin_custom", "BCH");
                }
            }
        }
    }

    // coinmarketcap changed their API a lot
    private static void migrateCoinMarketCapV2() {
        int[] widgetIds = WidgetApplication.getInstance().getWidgetIds();
        for (int widgetId : widgetIds) {
            Prefs prefs = new Prefs(widgetId);
            String exchange = prefs.getValue("exchange");
            if ("COINMARKETCAP".equals(exchange)) {
                String exchangeCoinName = prefs.getExchangeCoinName();
                if ("iota".equals(exchangeCoinName)) {
                    prefs.setValue("coin_custom", "MIOTA");
                } else {
                    prefs.setValue("coin_custom", prefs.getCoin().name());
                }
            }
        }
    }


    // spelled quoine wrong, so migrate users who have the prior bad spelling
    private static void migrateQuoine() {
        int[] widgetIds = WidgetApplication.getInstance().getWidgetIds();
        for (int widgetId : widgetIds) {
            Prefs prefs = new Prefs(widgetId);
            String exchange = prefs.getValue("exchange");
            if ("QUIONE".equals(exchange)) {
                prefs.setValue("exchange", Exchange.QUOINE.name());
            }
        }
    }

    // Certain exchanges have different names for coins and currencies than the rest.
    // Instead of hard coding these, we will now have these custom values populated from the JSON file,
    // and do a migration for existing users who already have widgets with these values.
    private static void migrateExchangeCoinAndCurrencyNames() {
        int[] widgetIds = WidgetApplication.getInstance().getWidgetIds();
        for (int widgetId : widgetIds) {
            Prefs prefs = new Prefs(widgetId);
            try {
                Exchange exchange = prefs.getExchange();
                Coin coin = prefs.getCoin();
                Currency currency = prefs.getCurrency();
                String coinName = null;
                String currencyName = null;
                switch (exchange) {
                    case BIT2C:
                        if (BTC == coin) coinName = "Btc";
                        if (BCH == coin) coinName = "Bch";
                        if (LTC == coin) coinName = "Ltc";
                        break;
                    case BITBAY:
                        if (BCH == coin) coinName = "BCC";
                        break;
                    case BITFINEX:
                        if (DASH == coin) coinName = "dsh";
                        if (IOTA == coin) coinName = "iot";
                        break;
                    case BITMARKET24:
                        if (BCH == coin) coinName = "BCC";
                        break;
                    case BITMARKETPL:
                        if (BCH == coin) coinName = "BCC";
                        break;
                    case BITTREX:
                        if (BCH == coin) coinName = "BCC";
                        if (USD == currency) currencyName = "USDT";
                        break;
                    case INDEPENDENT_RESERVE:
                        if (BTC == coin) coinName = "xbt";
                        break;
                    case ITBIT:
                        if (BTC == coin) coinName = "XBT";
                        break;
                    case KOINEX:
                        if (IOTA == coin) coinName = "MIOTA";
                        break;
                    case KRAKEN:
                        if (BTC == coin) coinName = "XBT";
                        break;
                    case LUNO:
                        if (BTC == coin) coinName = "XBT";
                        break;
                    case PARIBU:
                        if (TRY == currency) currencyName = "TL";
                        break;
                    case POLONIEX:
                        if (USD == currency) currencyName = "USDT";
                        break;
                    case WEX:
                        if (DASH == coin) coinName = "DSH";
                        break;
                }
                prefs.setExchangeValues(coinName, currencyName);
            } catch (Exception e) {
                // user's exchange no longer exists
            }
        }
    }

}
