package org.example.dto.admin;

import java.util.Map;

public class AdminStatsDto {
    private int totalUsers;
    private int verifiedUsers;
    private int usersWithApiKeys;
    private int totalApiCalls;
    private int activeUsers24h;
    private int activeUsers7d;
    private Map<String, Integer> planDistribution;

    public AdminStatsDto() {}

    public AdminStatsDto(int totalUsers, int verifiedUsers, int usersWithApiKeys, int totalApiCalls, 
                        int activeUsers24h, int activeUsers7d, Map<String, Integer> planDistribution) {
        this.totalUsers = totalUsers;
        this.verifiedUsers = verifiedUsers;
        this.usersWithApiKeys = usersWithApiKeys;
        this.totalApiCalls = totalApiCalls;
        this.activeUsers24h = activeUsers24h;
        this.activeUsers7d = activeUsers7d;
        this.planDistribution = planDistribution;
    }

    public int getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(int totalUsers) {
        this.totalUsers = totalUsers;
    }

    public int getVerifiedUsers() {
        return verifiedUsers;
    }

    public void setVerifiedUsers(int verifiedUsers) {
        this.verifiedUsers = verifiedUsers;
    }

    public int getUsersWithApiKeys() {
        return usersWithApiKeys;
    }

    public void setUsersWithApiKeys(int usersWithApiKeys) {
        this.usersWithApiKeys = usersWithApiKeys;
    }

    public int getTotalApiCalls() {
        return totalApiCalls;
    }

    public void setTotalApiCalls(int totalApiCalls) {
        this.totalApiCalls = totalApiCalls;
    }

    public int getActiveUsers24h() {
        return activeUsers24h;
    }

    public void setActiveUsers24h(int activeUsers24h) {
        this.activeUsers24h = activeUsers24h;
    }

    public int getActiveUsers7d() {
        return activeUsers7d;
    }

    public void setActiveUsers7d(int activeUsers7d) {
        this.activeUsers7d = activeUsers7d;
    }

    public Map<String, Integer> getPlanDistribution() {
        return planDistribution;
    }

    public void setPlanDistribution(Map<String, Integer> planDistribution) {
        this.planDistribution = planDistribution;
    }
} 
 
 