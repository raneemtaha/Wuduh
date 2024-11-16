package wuduh.taskAutomation;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import org.json.JSONObject;
import org.testng.annotations.Test;

import io.restassured.response.Response;

public class AppTest {

	// Defind the base URL for all services (Cases) in below
	static private String BASE_URL = "https://api.coindesk.com";
	static private String ACCEPT_HEADER = "*/*";
	static private String Accept_Encoding = "gzip, deflate, br";
	static private String Connection = "keep-alive";

	@Test
	public void T001_schemaValidation() {
		Response response;

		response = excuteSerice("/v1/bpi/currentprice.json");
		String responseBody = response.getBody().asString();

		JSONObject jsonResponse = new JSONObject(responseBody);

		if (jsonResponse.has("disclaimer")) {
			System.out.println("Disclaimer field exists.");
		} else {
			throw new AssertionError("Error: 'disclaimer' field is missing from the schema.");
		}

		if (jsonResponse.has("chartName")) {
			System.out.println("ChartName field exists.");
		} else {
			throw new AssertionError("Error: 'chartName' field is missing from the schema.");
		}

		if (jsonResponse.has("bpi")) {
			JSONObject bpi = jsonResponse.getJSONObject("bpi");

			String[] currencies = { "USD", "GBP", "EUR" };
			for (String currency : currencies) {
				if (bpi.has(currency)) {
					JSONObject currencyObj = bpi.getJSONObject(currency);

					if (!currencyObj.has("code") || !currencyObj.has("symbol") || !currencyObj.has("rate")
							|| !currencyObj.has("description") || !currencyObj.has("rate_float")) {
						throw new AssertionError("Error: Missing fields in 'bpi' for currency " + currency);
					}
					System.out.println(currency + " currency schema is valid.");
				} else {
					throw new AssertionError("Error: Missing currency " + currency + " in 'bpi'.");
				}
			}
		} else {
			throw new AssertionError("Error: 'bpi' field is missing from the schema.");
		}

		if (jsonResponse.has("time")) {
			JSONObject time = jsonResponse.getJSONObject("time");

			String[] timeAtt = { "updated", "updatedISO", "updateduk" };
			for (String att : timeAtt) {
				if (time.has(att)) {
					System.out.println(timeAtt + " field exists.");

				} else {
					throw new AssertionError("Error: Missing time=Attribute " + att + " in 'time'.");
				}
			}
		} else {
			throw new AssertionError("Error: 'time' field is missing from the schema.");
		}

		System.out.println("Schema validation passed successfully.");
	}

	@Test
	public void T002_validateSuccessResponseCodes() {

		Response response;

		response = excuteSerice("/v1/bpi/currentprice.json");
		assertThat(response.getStatusCode(), equalTo(200));

	}

	@Test
	public void T003_dataVerification() {

		Response response;

		response = excuteSerice("/v1/bpi/currentprice.json");
		String responseBody = response.getBody().asString();

		JSONObject jsonResponse = new JSONObject(responseBody);

		JSONObject bpi = jsonResponse.getJSONObject("bpi");

		String[] currencies = { "USD", "GBP", "EUR" };
		String[] att = { "code", "description" };
		String[] attValueDes = { "United States Dollar", "British Pound Sterling", "Euro" };
		for (int i = 0; i < currencies.length; i++) {
			if (bpi.has(currencies[i])) {

				JSONObject currencyObj = bpi.getJSONObject(currencies[i]);

				for (int j = 0; j < att.length;) {
					if (currencyObj.get(att[j]).equals(currencies[i])) {
						if (currencyObj.get(att[j + 1]).equals(attValueDes[i])) {
							break;
						} else {
							throw new AssertionError("Error: Wrong description for " + currencies[i] + " currency");
						}

					} else {
						throw new AssertionError("Error: Wrong " + currencies[i] + " code");
					}
				}
			} else {
				throw new AssertionError("Error: Missing " + att[i] + " in 'bpi' for currency " + currencies[i]);

			}

		}
	}

	@Test
	public void T004_validateResponseTime() {
		Response response;
		response = excuteSerice("/v1/bpi/currentprice.json");
		int actualResponseTime = (int) response.getTime();
		assertThat("Response time exceeded the maximum allowable limit", actualResponseTime, lessThanOrEqualTo(2000));
	}

	private Response excuteSerice(String path) {
		String url = BASE_URL + path;
		return given().header("Accept", ACCEPT_HEADER).header("Accept-Encoding", Accept_Encoding)
				.header("Connection", Connection).log().all().get(url)

				.then().log().all().extract().response();
	}

}
