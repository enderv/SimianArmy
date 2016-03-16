package com.netflix.simianarmy.basic.chaos;

/**
 * Created by Chris on 3/15/2016.
 */

import java.io.IOException;

import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.simianarmy.MonkeyConfiguration;
import com.netflix.simianarmy.chaos.ChaosCrawler.InstanceGroup;
import com.netflix.simianarmy.chaos.ChaosType;

public class BasicChaosSlackNotifier {
    /** The Constant LOGGER. */
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicChaosSlackNotifier.class);

    private final MonkeyConfiguration cfg;
    private final CloseableHttpClient httpSlackClient;

    public BasicChaosSlackNotifier(MonkeyConfiguration cfg) {
        this.cfg = cfg;
        this.httpSlackClient = HttpClients.createDefault();
    }

    public void sendSlackTerminationNotice(InstanceGroup group, String instanceId, ChaosType chaosType){
        String toChannel = cfg.getStr("simianarmy.chaos.notification.global.slackChannel");
        if (StringUtils.isBlank(toChannel)) {
            LOGGER.warn("Global Slack Channel Was not set");
            return;
        }
        LOGGER.info("Sending notification to channel {}", toChannel);
        HttpPost httpPost = new HttpPost("http://targethost/login");
        JSONObject obj = new JSONObject();
        try {
            String body = String.format("Instance %s of %s %s is being terminated by Chaos monkey using %s.",
                    instanceId, group.type(), group.name(), chaosType.getKey());
            obj.put("message",body);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try{
            StringEntity params = new StringEntity(obj.toString());
            httpPost.addHeader("content-type", "application/json");
            httpPost.setEntity(params);
            httpSlackClient.execute(httpPost);
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

}
