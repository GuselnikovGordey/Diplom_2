package praktikum;

import client.*;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import model.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

import static org.apache.http.HttpStatus.*;
import static constant.Constant.*;

public class CreateUserTest {

    private String randomString = RandomStringUtils.randomAlphanumeric(5);
    private String password = RandomStringUtils.randomNumeric(4);
    private String name = RandomStringUtils.randomAlphabetic(6);

    private String[] mailCompanies = new String[]{"yandex", "mail", "rambler"};
    private int randomMailCompany = new Random().nextInt(mailCompanies.length);
    private String email = randomString + "@" + mailCompanies[randomMailCompany] + ".ru";

    Response responseCreateUser;

    @Before
    public void setUp() {
        RestAssured.baseURI = BASE_URI;
    }

    @After
    public void clear() {
        if (responseCreateUser.statusCode() == SC_OK) {
            LoginUser loginObject = Login.createObjectLogin(email, password);
            Response responseLoginCourier = User.sendPostRequestAuthLogin(loginObject);
            LoginResponse loginResponse = Login.deserialization(responseLoginCourier);
            String accessToken = loginResponse.getAccessToken();

            Response responseDeleteCourier = User.sendDeleteRequestAuthUser(accessToken);
            Checks.checkExpectedResult(responseDeleteCourier, SC_ACCEPTED, EXPECTED_RESULT_TRUE);
        }
    }

    @Test
    @DisplayName("Создать уникального пользователя")
    public void createNewUser() {
        CreateUser user = User.createObjectUser(email, password, name);
        responseCreateUser = User.sendPostRequestAuthRegister(user);
        Checks.checkExpectedResult(responseCreateUser, SC_OK, EXPECTED_RESULT_TRUE);
    }

    @Test
    @DisplayName("Создать пользователя, который уже зарегистрирован")
    public void createAlreadyExistsUser() {
        CreateUser user = User.createObjectUser(DEFAULT_EMAIL, password, name);
        responseCreateUser = User.sendPostRequestAuthRegister(user);
        Checks.checkErrorMessage(responseCreateUser, SC_FORBIDDEN, EXPECTED_RESULT_FALSE, ALREADY_EXISTS);
    }

    @Test
    @DisplayName("Создать пользователя и не заполнить одно из обязательных полей")
    public void createUserWithoutPassword() {
        CreateUser user = User.createObjectUser(email, EMPTY_PASSWORD, name);
        responseCreateUser = User.sendPostRequestAuthRegister(user);
        Checks.checkErrorMessage(responseCreateUser, SC_FORBIDDEN, EXPECTED_RESULT_FALSE, REQUIRED_FIELDS);
    }
}
