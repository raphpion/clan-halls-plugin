package net.clanhalls.plugin.web;

import net.clanhalls.plugin.ClanHallsConfig;
import net.clanhalls.plugin.beans.MemberActivityReport;
import net.clanhalls.plugin.beans.MembersListReport;
import net.clanhalls.plugin.beans.SettingsReport;
import net.clanhalls.plugin.ClanHallsChatMessenger;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import okhttp3.*;

import javax.inject.Inject;
import java.text.DateFormat;
import java.util.function.Consumer;

@Slf4j
public class ClanHallsClient {
    @Inject
    private OkHttpClient okHttpClient;

    private Gson gson;

    @Inject
    private Client client;

    @Inject
    private ClanHallsConfig config;

    @Inject
    private ClanHallsChatMessenger messenger;

    @Inject
    public ClanHallsClient(Gson gson)
    {
        this.gson = gson.newBuilder()
                .setDateFormat(DateFormat.FULL, DateFormat.FULL)
                .create();
    }

    public void sendSettingsReport(SettingsReport settingsReport) {
        Request request = createRequest(settingsReport, HttpMethod.POST, "webhooks", "clans", "settings-report");
        sendRequest(request, this::sendSettingsReportCallback);
    }

    public void sendMembersListReport(MembersListReport membersListReport) {
        Request request = createRequest(membersListReport, HttpMethod.POST, "webhooks", "clans", "members-list-report");
        sendRequest(request, this::sendMembersListReportCallback);
    }

    public void sendMemberActivityReport(MemberActivityReport memberActivityReport) {
        Request request = createRequest(memberActivityReport, HttpMethod.POST, "webhooks", "clans", "member-activity-report");
        sendRequest(request, this::sendMemberActivityReportCallback);
    }

    private void sendSettingsReportCallback(Response response) {
        if (!response.isSuccessful()) {
            if (genericErrorsCallback(response)) return;

            messenger.send("Unexpected error while sending settings report.", ClanHallsChatMessenger.ERROR);
            return;
        }

        messenger.send("Settings report sent successfully!", ClanHallsChatMessenger.SUCCESS);
    }

    private void sendMembersListReportCallback(Response response) {
        if (!response.isSuccessful()) {
            if (genericErrorsCallback(response)) return;

            messenger.send("Unexpected error while sending members list report.", ClanHallsChatMessenger.ERROR);
            return;
        }

        messenger.send("Members list report sent successfully!", ClanHallsChatMessenger.SUCCESS);
    }

    private void sendMemberActivityReportCallback(Response response) {
        if (!response.isSuccessful()) {
            if (genericErrorsCallback(response)) return;

            messenger.send("Unexpected error while sending member activity report.", ClanHallsChatMessenger.ERROR);
            return;
        }

        messenger.send("Member activity report sent successfully!", ClanHallsChatMessenger.SUCCESS);
    }

    private boolean genericErrorsCallback(Response response) {
        switch (response.code()) {
            case 401:
                messenger.send("Invalid credentials. Please verify your configuration.", ClanHallsChatMessenger.ERROR);
                return true;

            case 500:
                messenger.send("A server error has occured. Please try again later.", ClanHallsChatMessenger.ERROR);
                return true;

            default:
                return false;
        }
    }

    private void sendRequest(Request request, Consumer<Response> consumer) {
        sendRequest(request, new ClanHallsCallback(consumer));
    }

    void sendRequest(Request request, Callback callback)
    {
        okHttpClient.newCall(request).enqueue(callback);
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
                .url(url);

        switch (method) {
            case POST:
                return requestBuilder.post(body).build();
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
}
