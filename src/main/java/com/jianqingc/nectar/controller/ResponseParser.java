package com.jianqingc.nectar.controller;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Jianqing Chen on 2016/10/1.
 * The ResponseParser is designed to parse the response of Login, List Volumes and List Instances HTTP requests.
 * Store the response into the SharedPreferences after parsing.
 */
public class ResponseParser {
    private static ResponseParser mInstance;
    private Context mApplicationContext;
    private String tenant_id;


    public static ResponseParser getInstance(Context context) {
        if (mInstance == null)
            mInstance = new ResponseParser(context);
        return mInstance;
    }
    public ResponseParser(Context context){
        this.mApplicationContext = context.getApplicationContext();
    }

    public void loginParser(JSONObject loginResponse){
        String expires = null;
        String tokenId = null;
        String tenantName = null;
        String tenantId = null;
        String tenantDescription = null;
        String username = null;
        String dnsServiceURL = null;
        String computeServiceURL = null;
        String networkServiceURL = null;
        String volumeV2ServiceURL = null;
        String s3ServiceURL = null;
        String alarmingServiceURL = null;
        String imageServiceURL = null;
        String meteringServiceURL = null;
        String cloudformationServiceURL = null;
        String applicationCatalogServiceURL = null;
        String volumeV1ServiceURL = null;
        String ec2ServiceURL = null;
        String orchestrationServiceURL = null;
        String objectStorageServiceURL = null;
        try {
            JSONObject accessInfo = loginResponse.getJSONObject("access");
            JSONArray serviceCatalog = accessInfo.getJSONArray("serviceCatalog");
            JSONObject token = accessInfo.getJSONObject("token");
            JSONObject user = accessInfo.getJSONObject("user");
            JSONObject tenant = token.getJSONObject("tenant");
            expires = token.getString("expires");
            tokenId = token.getString("id");
            tenantName= tenant.getString("name");
            tenantId = tenant.getString("id");
            tenant_id = tenantId;
            tenantDescription= tenant.getString("description");
            username = user.getString("username");
            dnsServiceURL = serviceCatalog.getJSONObject(0).getJSONArray("endpoints").getJSONObject(0).getString("publicURL");
            computeServiceURL = serviceCatalog.getJSONObject(1).getJSONArray("endpoints").getJSONObject(0).getString("publicURL");
            networkServiceURL = serviceCatalog.getJSONObject(2).getJSONArray("endpoints").getJSONObject(0).getString("publicURL");
            volumeV2ServiceURL = serviceCatalog.getJSONObject(3).getJSONArray("endpoints").getJSONObject(0).getString("publicURL");
            s3ServiceURL = serviceCatalog.getJSONObject(4).getJSONArray("endpoints").getJSONObject(0).getString("publicURL");
            alarmingServiceURL = serviceCatalog.getJSONObject(6).getJSONArray("endpoints").getJSONObject(0).getString("publicURL");

            System.out.println("alarm "+alarmingServiceURL);


            //imageServiceURL = serviceCatalog.getJSONObject(18).getJSONArray("endpoints").getJSONObject(0).getString("publicURL");
            imageServiceURL = "http://glance.rc.nectar.org.au:9292/";
            System.out.println("image "+imageServiceURL);
            objectStorageServiceURL = "https://swift.rc.nectar.org.au:8888/v1/AUTH_2296c4349965425f83befb6a7038a0ce";

            meteringServiceURL = serviceCatalog.getJSONObject(7).getJSONArray("endpoints").getJSONObject(0).getString("publicURL");
            System.out.println("metering "+ meteringServiceURL);
            cloudformationServiceURL = serviceCatalog.getJSONObject(8).getJSONArray("endpoints").getJSONObject(0).getString("publicURL");
            applicationCatalogServiceURL = serviceCatalog.getJSONObject(9).getJSONArray("endpoints").getJSONObject(0).getString("publicURL");
            volumeV1ServiceURL = serviceCatalog.getJSONObject(10).getJSONArray("endpoints").getJSONObject(0).getString("publicURL");
            ec2ServiceURL = serviceCatalog.getJSONObject(11).getJSONArray("endpoints").getJSONObject(0).getString("publicURL");
            orchestrationServiceURL = serviceCatalog.getJSONObject(12).getJSONArray("endpoints").getJSONObject(0).getString("publicURL");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        SharedPreferences sharedPreferences =  mApplicationContext.getSharedPreferences("nectar_android", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("expires",expires);
        editor.putString("tokenId", tokenId);
        System.out.println(tokenId);
        editor.putString("tenantName", tenantName);
        editor.putString("tenantId",tenantId);
        editor.putString("tenantDescription", tenantDescription);
        editor.putString("username", username);
        editor.putString("dnsServiceURL", dnsServiceURL);
        editor.putString("computeServiceURL", computeServiceURL);
        editor.putString("networkServiceURL", networkServiceURL);
        editor.putString("volumeV2ServiceURL", volumeV2ServiceURL);
        editor.putString("s3ServiceURL", s3ServiceURL);
        editor.putString("alarmingServiceURL", alarmingServiceURL);
        editor.putString("imageServiceURL", imageServiceURL);
        editor.putString("meteringServiceURL", meteringServiceURL);
        editor.putString("cloudformationServiceURL", cloudformationServiceURL);
        editor.putString("applicationCatalogServiceURL", applicationCatalogServiceURL);
        editor.putString("volumeV1ServiceURL", volumeV1ServiceURL);
        editor.putString("ec2ServiceURL", ec2ServiceURL);
        editor.putString("orchestrationServiceURL", orchestrationServiceURL);
        editor.putString("objectStorageServiceURL", objectStorageServiceURL);
        editor.apply();
        }


    public JSONArray listInstance(String response){
        JSONArray resultArray= new JSONArray();
        try {
            JSONObject responseJSON = new JSONObject(response);
            JSONArray serverArray= responseJSON.getJSONArray("servers");
            for (int i=0;i<serverArray.length();i++){
                JSONObject instanceObject = (JSONObject) serverArray.get(i);
                JSONObject resultObject = new JSONObject();
                resultObject.put("instanceId", instanceObject.getString("id"));
                resultObject.put("instanceStatus", instanceObject.getString("status"));
                resultObject.put("instanceName", instanceObject.getString("name"));
                resultObject.put("keyName", instanceObject.getString("key_name"));
                resultObject.put("instanceCreatedTime", instanceObject.getString("created"));
                resultObject.put("instanceUpdatedTime", instanceObject.getString("updated"));
                resultObject.put("tenantID", instanceObject.getString("tenant_id"));
                resultObject.put("IPv4Address", instanceObject.getString("accessIPv4"));
                resultObject.put("zone", instanceObject.getString("OS-EXT-AZ:availability_zone"));
                resultArray.put(i,resultObject);
            }
            return resultArray;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultArray;
    }


    public JSONArray listAlarm(String response){
        JSONArray resultArray=new JSONArray();
        try{
            JSONArray serverArray =new JSONArray(response);



            for(int i = 0 ; i < serverArray.length();i++)
            {
                JSONObject alarmObject = serverArray.getJSONObject(i);
                JSONObject thresholdRule =alarmObject.getJSONObject("gnocchi_resources_threshold_rule");
                JSONObject resultObject = new JSONObject();
                resultObject.put("AlarmName", alarmObject.getString("name"));
                resultObject.put("severity",alarmObject.getString("severity"));
                resultObject.put("state",alarmObject.getString("state"));
                resultObject.put("alarmID",alarmObject.getString("alarm_id"));
                resultObject.put("metric",thresholdRule.getString("metric"));
                resultObject.put("aggregation_method",thresholdRule.getString("aggregation_method"));
                resultObject.put("threshold",thresholdRule.getInt("threshold"));
                resultObject.put("granularity",thresholdRule.getInt("granularity"));
                resultObject.put("evaluation_periods",thresholdRule.getInt("evaluation_periods"));
                resultObject.put("description",alarmObject.getString("description"));
                resultObject.put("repeat_actions",alarmObject.getString("repeat_actions"));
                resultObject.put("resource_type",thresholdRule.getString("resource_type"));
                resultObject.put("comparison_operator",thresholdRule.getString("comparison_operator"));
                resultObject.put("type",alarmObject.getString("type"));
                resultArray.put(i,resultObject);

            }

            return resultArray;

        } catch(JSONException e) {
            e.printStackTrace();
        }
        return resultArray;
    }

    public JSONArray listcontainer(String response){
        JSONArray resultArray=new JSONArray();
        try{
            JSONArray serverArray =new JSONArray(response);

            for(int i = 0 ; i < serverArray.length();i++)
            {
                JSONObject containerObject = serverArray.getJSONObject(i);

                JSONObject resultObject = new JSONObject();
                resultObject.put("containerName", containerObject.getString("name"));
                resultObject.put("count",containerObject.getInt("count"));
                resultObject.put("bytes",(containerObject.getDouble("bytes")/1024.0/1024.0));
                resultArray.put(i,resultObject);
            }

            return resultArray;

        } catch(JSONException e) {
            e.printStackTrace();
        }
        return resultArray;
    }

    public JSONArray listObject(String response){
        JSONArray resultArray=new JSONArray();
        try{
            JSONArray serverArray =new JSONArray(response);



            for(int i = 0 ; i < serverArray.length();i++)
            {

                JSONObject Object = serverArray.getJSONObject(i);

                JSONObject resultObject = new JSONObject();
                resultObject.put("objectName", Object.getString("name"));
                resultObject.put("last_modified",Object.getString("last_modified"));

                resultArray.put(i,resultObject);

            }

            return resultArray;

        } catch(JSONException e) {
            e.printStackTrace();
        }
        return resultArray;
    }



    public JSONArray listImage(String response){
        JSONArray resultArray= new JSONArray();
        try {
            JSONObject responseJSON = new JSONObject(response);
            JSONArray serverArray= responseJSON.getJSONArray("images");
            for (int i=0;i<serverArray.length();i++){
                JSONObject instanceObject = (JSONObject) serverArray.get(i);
                JSONObject resultObject = new JSONObject();
                resultObject.put("imageId", instanceObject.getString("id"));
                resultObject.put("imageStatus", instanceObject.getString("status"));
                resultObject.put("imageName", instanceObject.getString("name"));
                resultObject.put("imageOwner", instanceObject.getString("owner"));
                resultObject.put("imageVisibility", instanceObject.getString("visibility"));
                resultObject.put("imageProtected", instanceObject.getString("protected"));
                resultObject.put("imageChecksum", instanceObject.getString("checksum"));
                resultObject.put("imageCreatedTime", instanceObject.getString("created_at"));
                resultObject.put("imageUpdatedTime", instanceObject.getString("updated_at"));
                resultObject.put("imageSize", instanceObject.getString("size"));
                resultObject.put("imageDiskFormat", instanceObject.getString("disk_format"));
                resultObject.put("imageContainerFormat", instanceObject.getString("container_format"));
                resultObject.put("imageMinDisk", instanceObject.getString("min_disk"));
                resultObject.put("imageType", instanceObject.getString("image_type"));
                resultArray.put(i,resultObject);
            }
            return resultArray;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultArray;
    }

    public JSONArray listImageOfficial(String response){
        JSONArray resultArray= new JSONArray();
        try {
            JSONObject responseJSON = new JSONObject(response);
            JSONArray serverArray= responseJSON.getJSONArray("images");
            for (int i=0;i<serverArray.length();i++){
                JSONObject instanceObject = (JSONObject) serverArray.get(i);
                JSONObject resultObject = new JSONObject();
                resultObject.put("imageId", instanceObject.getString("id"));
                resultObject.put("imageStatus", instanceObject.getString("status"));
                resultObject.put("imageName", instanceObject.getString("name"));
                resultObject.put("imageOwner", instanceObject.getString("owner"));
                resultObject.put("imageVisibility", instanceObject.getString("visibility"));
                resultObject.put("imageProtected", instanceObject.getString("protected"));
                resultObject.put("imageChecksum", instanceObject.getString("checksum"));
                resultObject.put("imageCreatedTime", instanceObject.getString("created_at"));
                resultObject.put("imageUpdatedTime", instanceObject.getString("updated_at"));
                resultObject.put("imageSize", instanceObject.getString("size"));
                resultObject.put("imageDiskFormat", instanceObject.getString("disk_format"));
                resultObject.put("imageContainerFormat", instanceObject.getString("container_format"));
                resultObject.put("imageMinDisk", instanceObject.getString("min_disk"));
                resultArray.put(i,resultObject);
            }
            return resultArray;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultArray;
    }

    public JSONObject listImageDetail(String response){
        JSONObject resultObject = new JSONObject();
        try {
            JSONObject responseJSON = new JSONObject(response);

                resultObject.put("imageId", responseJSON.getString("id"));
                resultObject.put("imageStatus", responseJSON.getString("status"));
                resultObject.put("imageName", responseJSON.getString("name"));
                resultObject.put("imageOwner", responseJSON.getString("owner"));
                resultObject.put("imageVisibility", responseJSON.getString("visibility"));
                resultObject.put("imageProtected", responseJSON.getString("protected"));
                resultObject.put("imageChecksum", responseJSON.getString("checksum"));
                resultObject.put("imageCreatedTime", responseJSON.getString("created_at"));
                resultObject.put("imageUpdatedTime", responseJSON.getString("updated_at"));
                resultObject.put("imageSize", responseJSON.getString("size"));
                resultObject.put("imageDiskFormat", responseJSON.getString("disk_format"));
                resultObject.put("imageContainerFormat", responseJSON.getString("container_format"));
                resultObject.put("imageMinDisk", responseJSON.getString("min_disk"));

            return resultObject;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultObject;
    }

    public JSONArray listKeyPair(String response){
        JSONArray resultArray= new JSONArray();
        try {
            JSONObject responseJSON = new JSONObject(response);
            JSONArray serverArray= responseJSON.getJSONArray("keypairs");
            for (int i=0;i<serverArray.length();i++){
                JSONObject instanceObject = (JSONObject) serverArray.get(i);
                JSONObject kpObject= instanceObject.getJSONObject("keypair");
                JSONObject resultObject = new JSONObject();
                resultObject.put("kpPublicKey", kpObject.getString("public_key"));
                resultObject.put("kpName", kpObject.getString("name"));
                resultObject.put("kpFingerPrint", kpObject.getString("fingerprint"));
                resultArray.put(i,resultObject);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultArray;
    }

    public JSONObject listkeypairDetail(String response){
        JSONObject resultObject = new JSONObject();
        try {
            JSONObject responseJSON = new JSONObject(response);
            JSONObject keyPair = (JSONObject) responseJSON.getJSONObject("keypair");
            resultObject.put("kpPublicKey", keyPair.getString("public_key"));
            resultObject.put("kpName", keyPair.getString("name"));
            resultObject.put("kpFingerPrint", keyPair.getString("fingerprint"));
            resultObject.put("kpCreateTime", keyPair.getString("created_at"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultObject;
    }

    public JSONArray listAvabilityZone(String response){
        JSONArray resultArray= new JSONArray();
        try {
            JSONObject responseJSON = new JSONObject(response);
            JSONArray serverArray= responseJSON.getJSONArray("availabilityZoneInfo");
            for (int i=0;i<serverArray.length();i++){
                JSONObject instanceObject = (JSONObject) serverArray.get(i);
                JSONObject state= instanceObject.getJSONObject("zoneState");
                JSONObject resultObject = new JSONObject();
                resultObject.put("azHosts", instanceObject.getString("hosts"));
                resultObject.put("azName", instanceObject.getString("zoneName"));
                resultObject.put("azState", state.getBoolean("available"));
                resultArray.put(i,resultObject);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultArray;
    }

    public JSONArray listFlavor(String response){
        JSONArray resultArray= new JSONArray();
        try {
            JSONObject responseJSON = new JSONObject(response);
            JSONArray flavorArray= responseJSON.getJSONArray("flavors");
            for (int i=0;i<flavorArray.length();i++) {
                JSONObject flavorObject = (JSONObject) flavorArray.get(i);
                JSONObject resultObject = new JSONObject();
                resultObject.put("flavorId", flavorObject.getString("id"));
                resultObject.put("flavorName", flavorObject.getString("name"));
                resultArray.put(i, resultObject);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultArray;
    }

    public JSONArray listSecurityGroup(String response){
        JSONArray resultArray= new JSONArray();
        try {
            JSONObject responseJSON = new JSONObject(response);
            JSONArray securityArray= responseJSON.getJSONArray("security_groups");
            for (int i=0;i<securityArray.length();i++) {
                JSONObject sgObject = (JSONObject) securityArray.get(i);
                JSONObject resultObject = new JSONObject();
                resultObject.put("sgId", sgObject.getString("id"));
                resultObject.put("sgName", sgObject.getString("name"));
                resultObject.put("sgDescription", sgObject.getString("description"));
                resultObject.put("sgTenantID", sgObject.getString("tenant_id"));
                resultArray.put(i, resultObject);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultArray;
    }

    public JSONArray listRulesSG(String response){
        JSONArray resultArray= new JSONArray();
        try {
            JSONObject responseJSON = new JSONObject(response);
            JSONObject securityGroup= responseJSON.getJSONObject("security_group");
            JSONArray rules= securityGroup.getJSONArray("security_group_rules");
            for (int i=0;i<rules.length();i++) {
                JSONObject rule = (JSONObject) rules.get(i);
                JSONObject resultObject = new JSONObject();
                resultObject.put("ruleDirection", rule.getString("direction"));
                resultObject.put("ruleEtherType", rule.getString("ethertype"));
                resultObject.put("ruleProtocol", rule.getString("protocol"));
                resultObject.put("rulePortMin", rule.getString("port_range_min"));
                resultObject.put("rulePortMax", rule.getString("port_range_max"));
                resultObject.put("ruleRemoteIP", rule.getString("remote_ip_prefix"));
                resultObject.put("ruleRemoteG", rule.getString("remote_group_id"));
                resultObject.put("ruleID", rule.getString("id"));
                resultArray.put(i, resultObject);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultArray;
    }

    public JSONObject listVolumeDetail(String response){
        JSONObject resultObject = new JSONObject();
        try {
            JSONObject responseJSON = new JSONObject(response);
            JSONObject volume = (JSONObject) responseJSON.getJSONObject("volume");
            resultObject.put("vStatus", volume.getString("status"));
            resultObject.put("vID", volume.getString("id"));
            resultObject.put("vName", volume.getString("name"));
            resultObject.put("vCreate", volume.getString("created_at"));
            resultObject.put("vSize", volume.getString("size"));
            resultObject.put("vDescription", volume.getString("description"));
            resultObject.put("vZone", volume.getString("availability_zone"));
            resultObject.put("vBootable", volume.getString("bootable"));

            resultObject.put("vEncrypted", volume.getString("encrypted"));
            JSONArray attachArray=volume.getJSONArray("attachments");
            int numA=attachArray.length();
            resultObject.put("vnumA", numA);
            for(int j=0;j<numA;j++){
                JSONObject vObject= (JSONObject) attachArray.get(j);
                String serverID=vObject.getString("server_id");
                resultObject.put("server"+j, serverID);
                String device=vObject.getString("device");
                resultObject.put("device"+j, device);

            }

            //Add attaching servers latter

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultObject;
    }

    //listVolume
    public JSONArray listVolume(String response){
        JSONArray resultArray= new JSONArray();
        try {
            JSONObject responseJSON = new JSONObject(response);
            JSONArray volumesArray= responseJSON.getJSONArray("volumes");
            for (int i=0;i<volumesArray.length();i++){
                JSONObject instanceObject = (JSONObject) volumesArray.get(i);
                JSONObject resultObject = new JSONObject();
                resultObject.put("volumeId", instanceObject.getString("id"));
                resultObject.put("volumeSize", instanceObject.getString("size"));
                resultObject.put("volumeStatus", instanceObject.getString("status"));
                resultObject.put("volumeName", instanceObject.getString("name"));
                resultObject.put("volumeDescription", instanceObject.getString("description"));
                resultObject.put("volumeAZ", instanceObject.getString("availability_zone"));

                JSONArray attachA=instanceObject.getJSONArray("attachments");
                int numA=attachA.length();
                resultObject.put("volumeNumAttach",numA);
                for(int j=0;j<numA;j++){
                    JSONObject object=(JSONObject) attachA.get(j);
                    String attachID=object.getString("attachment_id");
                    resultObject.put("vattach"+j, attachID);
                    String device=object.getString("server_id");
                    resultObject.put("vserver"+j, device);

                }

                resultArray.put(i,resultObject);
            }
            return resultArray;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultArray;
    }

    public JSONArray listVolumeType(String response){
        JSONArray resultArray= new JSONArray();
        try {
            JSONObject responseJSON = new JSONObject(response);
            JSONArray volumesArray= responseJSON.getJSONArray("volume_types");
            for (int i=0;i<volumesArray.length();i++){
                JSONObject instanceObject = (JSONObject) volumesArray.get(i);
                JSONObject resultObject = new JSONObject();
                resultObject.put("typeId", instanceObject.getString("id"));
                resultObject.put("typeName", instanceObject.getString("name"));

                resultArray.put(i,resultObject);
            }
            return resultArray;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultArray;
    }

    public JSONArray listSnapshot(String response){
        JSONArray resultArray= new JSONArray();
        try {
            JSONObject responseJSON = new JSONObject(response);
            JSONArray volumesArray= responseJSON.getJSONArray("snapshots");
            for (int i=0;i<volumesArray.length();i++){
                JSONObject instanceObject = (JSONObject) volumesArray.get(i);
                JSONObject resultObject = new JSONObject();
                resultObject.put("snapID", instanceObject.getString("id"));
                resultObject.put("snapSize", instanceObject.getString("size"));
                resultObject.put("snapStatus", instanceObject.getString("status"));
                resultObject.put("snapName", instanceObject.getString("name"));
                resultObject.put("snapDescription", instanceObject.getString("description"));

                resultArray.put(i,resultObject);
            }
            return resultArray;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultArray;
    }

    public JSONObject listVolumeSnapshotDetail(String response){
        JSONObject resultObject = new JSONObject();
        try {
            JSONObject responseJSON = new JSONObject(response);
            JSONObject volume = (JSONObject) responseJSON.getJSONObject("snapshot");
            resultObject.put("vStatus", volume.getString("status"));
            resultObject.put("vID", volume.getString("id"));
            resultObject.put("vName", volume.getString("name"));
            resultObject.put("vCreate", volume.getString("created_at"));
            resultObject.put("vSize", volume.getString("size"));
            resultObject.put("vDescription", volume.getString("description"));
            resultObject.put("vVolume", volume.getString("volume_id"));


            //Add attaching servers latter

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultObject;
    }

    public JSONArray listFloatingIP(String response){
        JSONArray resultArray = new JSONArray();

        try {
            JSONObject responseJSON = new JSONObject(response);



            JSONArray serverArray = responseJSON.getJSONArray("floatingips");
            for(int i=0; i<serverArray.length();i++)
            {
                JSONObject floatingObject = serverArray.getJSONObject(i);
                JSONObject resultObject = new JSONObject();
                resultObject.put("floatingID",floatingObject.getString("id"));
                resultObject.put("floating_ip_address",floatingObject.getString("floating_ip_address"));
                resultObject.put("floating_network_id",floatingObject.getString("floating_network_id"));
                resultObject.put("status",floatingObject.getString("status"));
                resultArray.put(i,resultObject);

            }

            return resultArray;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultArray;

    }

    public JSONArray listRouter(String response){
        JSONArray resultArray = new JSONArray();

        try {
            JSONObject responseJSON = new JSONObject(response);



            JSONArray serverArray = responseJSON.getJSONArray("routers");
            for(int i=0; i<serverArray.length();i++)
            {
                JSONObject routerObject = serverArray.getJSONObject(i);
                JSONObject resultObject = new JSONObject();

                JSONObject externalNetworkObject = routerObject.getJSONObject("external_gateway_info");

                JSONArray fixedipsArray = externalNetworkObject.getJSONArray(("external_fixed_ips"));


                resultObject.put("routerID",routerObject.getString("id"));
                resultObject.put("routerName",routerObject.getString("name"));
                resultObject.put("admin_state_up",routerObject.getString("admin_state_up"));
                resultObject.put("status",routerObject.getString("status"));
                resultObject.put("project_id",routerObject.getString("project_id"));

                resultObject.put("network_id", externalNetworkObject.getString("network_id"));
                resultObject.put("enable_snat", externalNetworkObject.getString("enable_snat"));

                resultObject.put("project_id","project_id");


                //the json object of internal array :external_fixed_ips
                for (int j=0; j< fixedipsArray.length();j++)
                {
                    JSONObject tempObject = fixedipsArray.getJSONObject(j);

                    resultObject.put("subnet_id",tempObject.getString("subnet_id"));
                    resultObject.put("ip_address",tempObject.getString("ip_address"));
                }
                resultArray.put(i,resultObject);

            }

            return resultArray;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultArray;

    }

    public JSONArray listNetwork(String response) {
        JSONArray resultArray = new JSONArray();

        try {
            JSONObject responseJSON = null;
            responseJSON = new JSONObject(response);
            JSONArray serverArray = responseJSON.getJSONArray("networks");
            int count = 0;
            for (int i = 0; i < serverArray.length(); i++)
            {
                JSONObject networksObject = serverArray.getJSONObject(i);
                JSONObject resultObject = new JSONObject();
                String tenant_id = networksObject.getString("tenant_id");
                if(tenant_id.equals(this.tenant_id)){


                    resultObject.put("networkName",networksObject.getString("name"));
                    resultObject.put("tenant_id",tenant_id);
                    resultObject.put("status",networksObject.getString("status"));
                    resultObject.put("shared",networksObject.getString("shared"));
                    resultObject.put("admin_state_up",networksObject.getString("admin_state_up"));
                    resultObject.put("networkid",networksObject.getString("id"));
                    resultObject.put("router_external",networksObject.getString("router:external"));
                    resultObject.put("mtu",networksObject.getString("mtu"));
                    JSONArray subnetArray = networksObject.getJSONArray("subnets");
                    String subnet_array = subnetArray.toString();
                    resultObject.put("subnet_array",subnet_array);
                    resultArray.put(count,resultObject);
                    count++;
                }

            }

            return resultArray;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultArray;
    }

    public JSONArray listSubnet(String response, String networkID){
        JSONArray resultArray = new JSONArray();

        try {
            JSONObject responseJSON = null;
            responseJSON = new JSONObject(response);
            JSONArray serverArray = responseJSON.getJSONArray("subnets");
            int count = 0;
            for (int i = 0; i < serverArray.length(); i++) {
                JSONObject subnetObject = serverArray.getJSONObject(i);
                JSONObject resultObject = new JSONObject();
                String network_id = subnetObject.getString("network_id");
                if (network_id.equals(networkID)) {

                    resultObject.put("subnetName", subnetObject.getString("name"));
                    resultObject.put("subnetID", subnetObject.getString("id"));
                    resultObject.put("cidr", subnetObject.getString("cidr"));
                    resultObject.put("network_id", subnetObject.getString("network_id"));
                    resultObject.put("ip_version", subnetObject.getString("ip_version"));
                    JSONArray allocation_pools_array = subnetObject.getJSONArray("allocation_pools");
                    String allocationString = allocation_pools_array.toString();
                    resultObject.put("allocation_pools", allocationString);
                    resultArray.put(count,resultObject);
                    count++;
                }
            }


            return resultArray;


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return resultArray;
    }



}
