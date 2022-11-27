import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import ru.netology.entity.Country;
import ru.netology.entity.Location;
import ru.netology.geo.GeoService;
import ru.netology.geo.GeoServiceImpl;
import ru.netology.i18n.LocalizationService;
import ru.netology.i18n.LocalizationServiceImpl;
import ru.netology.sender.MessageSender;
import ru.netology.sender.MessageSenderImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;


public class TestMessageSender {

    @ParameterizedTest
    @CsvSource({"172.10.54.5, Добро пожаловать", "96.4.23.74, Welcome"})
    void test_MessageSender_language_by_ip(String ipAddress, String message) {
        GeoService geoService = Mockito.mock(GeoService.class);
        Mockito.when(geoService.byIp(ipAddress)).thenReturn(new Location("SomeCity", Country.RUSSIA, "Street", 15));

        LocalizationService localizationService = Mockito.mock(LocalizationService.class);
        Mockito.when(localizationService.locale(Mockito.<Country>any())).thenReturn(message);

        MessageSender messageSender = new MessageSenderImpl(geoService, localizationService);

        Map<String, String> headers = new HashMap<String, String>();
        headers.put(MessageSenderImpl.IP_ADDRESS_HEADER, ipAddress);
        String expected = messageSender.send(headers);
        Assertions.assertEquals(expected, message);
    }

    @ParameterizedTest
    @MethodSource("params")
    void test_GeoServiceImpl_byIp_method(String ipAddress, Location location) {
        GeoService geoService = new GeoServiceImpl();
        Assertions.assertEquals(location.getCountry(), geoService.byIp(ipAddress).getCountry());
        Assertions.assertEquals(location.getCity(), geoService.byIp(ipAddress).getCity());
        Assertions.assertEquals(location.getStreet(), geoService.byIp(ipAddress).getStreet());
        Assertions.assertEquals(location.getBuiling(), geoService.byIp(ipAddress).getBuiling());

    }

    @Test
    void test_LocalizationServiceImpl_locale_method() {
        LocalizationService localizationService = new LocalizationServiceImpl();

        Assertions.assertEquals("Добро пожаловать", localizationService.locale(Country.RUSSIA));
        Assertions.assertEquals("Welcome", localizationService.locale(Country.USA));
        Assertions.assertNotEquals("Welcome", localizationService.locale(Country.RUSSIA));
    }

    @Test
    void test_forExceptionThrow_GeoServiceImpl_byCoordinate_method_() {
        double latitude = 55.7522;
        double longitude = 37.6156;
        GeoService geoService = new GeoServiceImpl();

        Assertions.assertThrows(RuntimeException.class, () -> geoService.byCoordinates(latitude, longitude));
    }

    private static Stream<Arguments> params() {
        return Stream.of(
                Arguments.of("127.0.0.1", new Location(null, null, null, 0)),
                Arguments.of("172.0.32.11", new Location("Moscow", Country.RUSSIA, "Lenina", 15)),
                Arguments.of("96.44.183.149", new Location("New York", Country.USA, " 10th Avenue", 32)),
                Arguments.of("172.0.0.1", new Location("Moscow", Country.RUSSIA, null, 0)),
                Arguments.of("96.0.0.1", new Location("New York", Country.USA, null, 0)));
    }


}
