package com.halloapp.ui.invites;

public class InviteCountAndRefreshTime {
    public int invitesRemaining;
    public long timeTillRefresh;

    public InviteCountAndRefreshTime(int invitesRemainingIn, long timeTillRefreshIn){
        invitesRemaining = invitesRemainingIn;
        timeTillRefresh = timeTillRefreshIn;
    }

    public InviteCountAndRefreshTime(){

    }

    public int getInvitesRemaining() {
        return invitesRemaining;
    }

    public long getTimeTillRefresh(){
        return timeTillRefresh;
    }

    public void setInviteRemaining(int invitesRemaining){
        this.invitesRemaining = invitesRemaining;
    }

    public void setTimeTillRefresh(long timeTillRefresh) {
        this.timeTillRefresh = timeTillRefresh;
    }
}
