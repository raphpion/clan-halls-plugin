package net.clanhalls.plugin.web;

import net.clanhalls.plugin.ClanHallsConfig;
import net.clanhalls.plugin.beans.*;
import net.clanhalls.plugin.ClanHallsChatMessenger;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import okhttp3.*;
import okio.BufferedSink;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.Base64;
import java.util.function.Consumer;

@Slf4j
public class ClanHallsClient {
    @Inject
    private OkHttpClient okHttpClient;

    private Gson gson;

    @Inject
    private ClanHallsConfig config;

    @Inject
    public ClanHallsClient(Gson gson)
    {
        this.gson = gson.newBuilder()
                .setDateFormat(DateFormat.FULL, DateFormat.FULL)
                .create();
    }

    public APIResponse<ClanInfo> getClan() {
        Request request = createRequest(HttpMethod.GET, "webhooks", "clans");
        return sendRequest(request, ClanInfo.class);
    }

    public APIResponse<ReportInfo> sendSettingsReport(SettingsReport settingsReport) {
        Request request = createRequest(settingsReport, HttpMethod.POST, "webhooks", "clans", "settings-report");
        return sendRequest(request, ReportInfo.class);
    }

    public APIResponse<ReportInfo> sendMembersListReport(MembersListReport membersListReport) {
        Request request = createRequest(membersListReport, HttpMethod.POST, "webhooks", "clans", "members-list-report");
        return sendRequest(request, ReportInfo.class);
    }

    public APIResponse<ReportInfo> sendMemberActivityReport(MemberActivityReport memberActivityReport) {
        Request request = createRequest(memberActivityReport, HttpMethod.POST, "webhooks", "clans", "member-activity-report");
        return sendRequest(request, ReportInfo.class);
    }

    private <T> APIResponse<T> sendRequest(Request request, Class<T> responseType) {
        log.info(request.url().toString());
        if (request.body() != null) {
            log.info(request.body().toString());
        }

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return APIResponse.failure(response.code(), "HTTP Error: " + response.code());
            }

            if (response.body() == null || response.body().contentLength() == 0) {
                return APIResponse.success(null);
            }

            String responseBody = response.body().string();
            T data = gson.fromJson(responseBody, responseType);
            return APIResponse.success(data);
        } catch (IOException e) {
            return APIResponse.failure(500, "Network error: " + e.getMessage());
        }
    }

    private Request createRequest(HttpMethod method, String... pathSegments) {
        HttpUrl url = buildUrl(pathSegments);

        Request.Builder requestBuilder = new Request.Builder()
                .header("User-Agent", "ClanHalls RuneLite Plugin")
                .header("Content-Type", "application/json")
                .header("Authorization", getBasicAuthHeader())
                .url(url);

        switch (method) {
            case GET:
                return requestBuilder.get().build();
            case DELETE:
                return requestBuilder.delete().build();
            default:
                throw new IllegalArgumentException("Invalid http method specified!");
        }
    }

    private Request createRequest(Object payload, HttpMethod method, String... pathSegments) {
        HttpUrl url = buildUrl(pathSegments);

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json charset=utf-8"),
                gson.toJson(payload)
        );

        Request.Builder requestBuilder = new Request.Builder()
                .header("User-Agent", "ClanHalls RuneLite Plugin")
                .header("Content-Type", "application/json")
                .header("Authorization", getBasicAuthHeader())
                .url(url);

        switch (method) {
            case POST:
                return requestBuilder.post(body).build();
            case PUT:
                return requestBuilder.put(body).build();
            case PATCH:
                return requestBuilder.patch(body).build();
            case DELETE:
                return requestBuilder.delete(body).build();
            default:
                throw new IllegalArgumentException("Invalid http method specified!");
        }
    }

    private HttpUrl buildUrl(String... pathSegments) {
        String apiBaseUrl = config.apiBaseUrl();
        HttpUrl parsedUrl = HttpUrl.parse(apiBaseUrl);
        if (parsedUrl == null) {
            throw new IllegalArgumentException("Invalid api base url: " + apiBaseUrl);
        }

        HttpUrl.Builder urlBuilder = new HttpUrl.Builder()
                .scheme(parsedUrl.scheme())
                .host(parsedUrl.host());

        if (parsedUrl.port() != HttpUrl.defaultPort(parsedUrl.scheme())) {
            urlBuilder.port(parsedUrl.port());
        }

        for (String pathSegment : parsedUrl.pathSegments())
        {
            urlBuilder.addPathSegment(pathSegment);
        }

        for (String pathSegment : pathSegments)
        {
            if (pathSegment.startsWith("?"))
            {
                String[] kv = pathSegment.substring(1).split("=");
                urlBuilder.addQueryParameter(kv[0], kv[1]);
            }
            else
            {
                urlBuilder.addPathSegment(pathSegment);
            }
        }

        return urlBuilder.build();
    }

    private String getBasicAuthHeader() {
        String credentials = config.clientId() + ":" + config.clientSecret();
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }
}
