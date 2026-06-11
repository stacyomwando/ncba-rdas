package com.ncba.rdas;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.ncba.rdas.client.CountryInfoSoapClient;
import java.util.Collections;
import static org.mockito.Mockito.when;

@SpringBootTest
class RdasApplicationTests {

    @MockBean
    CountryInfoSoapClient soapClient;

    @Test
    void contextLoads() {
        when(soapClient.fetchAllCountries()).thenReturn(Collections.emptyList());
        when(soapClient.fetchAllContinents()).thenReturn(Collections.emptyList());
        when(soapClient.fetchAllCurrencies()).thenReturn(Collections.emptyList());
        when(soapClient.fetchAllLanguages()).thenReturn(Collections.emptyList());
    }
}
