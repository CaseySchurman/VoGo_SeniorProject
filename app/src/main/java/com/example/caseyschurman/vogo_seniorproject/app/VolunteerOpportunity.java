package com.example.caseyschurman.vogo_seniorproject.app;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by casey.schurman on 3/1/2016.
 */
public class VolunteerOpportunity {
    private String mName;
    private ArrayList<String> mRelevantSkills;
    private String mOrganization;
    private String mAddress;

    public VolunteerOpportunity(String name, ArrayList<String> relevantSkills, String organization,
                                String address) {
        mName = name;
        mRelevantSkills = relevantSkills;
        mOrganization = organization;
        mAddress = address;
    }

    public String getName() {
        return mName;
    }

    public ArrayList<String> getRelevantSkills(){
        return mRelevantSkills;
    }

    public String getOrganization(){
        return mOrganization;
    }

    public String getAddress(){
        return mAddress;
    }
}
