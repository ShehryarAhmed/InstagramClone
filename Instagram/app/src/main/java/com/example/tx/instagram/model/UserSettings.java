package com.example.tx.instagram.model;

public class UserSettings {

    private User user;
    private UserAccountSetting userAccountSetting;

    public UserSettings() {
    }

    public UserSettings(User user, UserAccountSetting userAccountSetting) {
        this.user = user;
        this.userAccountSetting = userAccountSetting;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public UserAccountSetting getUserAccountSetting() {
        return userAccountSetting;
    }

    public void setUserAccountSetting(UserAccountSetting userAccountSetting) {
        this.userAccountSetting = userAccountSetting;
    }
}
