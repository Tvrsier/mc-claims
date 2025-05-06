package tvrsier.eu.mclaims.manager.roles;

public enum Permissions {
    // The permission enum will have an associated string value
    // that represents the permission string in the yaml file
    // The permission string will be used to check if the player has the permission
    BUILDING_ALLOWED("building-allowed"),
    VISITOR_ALLOWED("visitor-allowed"),
    MANAGE_SUBCLAIMS("manage-subclaims"),
    ACCEPT_VISITORS("accept-visitors"),
    INVITE("can-invite"),
    CHANGE_TEAM_NAME("can-change-team-name"),
    CHANGE_TEAM_ROLE("can-change-team-role"),
    BUILD("can-build"),
    ALLOW_BUILDING("can-allow-building"),
    REMOVE_MEMBER("can-remove-member"),
    ;

    Permissions(String s) {
        this.value = s;
    }

    public String getValue() {
        return value;
    }


    private final String value;
}
