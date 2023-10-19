package pt.tecnico.grpc.server;

/**
 * The Thresholds class stores the thresholds
 * for each moderation task belonging to a chat room.
 */
public class Thresholds {

    private double create_channel_threshold;
    private double delete_channel_threshold;
    private double delete_message_threshold;
    private double delete_user_threshold;
    private double edit_message_threshold;
    private double invite_user_threshold;
    private double pin_message_threshold;
    private double punish_user_threshold;
    private double unpin_message_threshold;

    public Thresholds(){
        this.create_channel_threshold = 0.3;
        this.delete_channel_threshold = 0.7;
        this.delete_message_threshold = 0.6;
        this.delete_user_threshold = 0.7;
        this.edit_message_threshold = 0.4;
        this.invite_user_threshold = 0.3;
        this.pin_message_threshold = 0.3;
        this.punish_user_threshold = 0.6;
        this.unpin_message_threshold = 0.4;
    }

    public double getCreateChannelThreshold() {
        return this.create_channel_threshold;
    }

    public void setCreateChannelThreshold(double threshold) {
        this.create_channel_threshold = threshold;
    }

    public double getDeleteChannelThreshold() {
        return this.delete_channel_threshold;
    }

    public void setDeleteChannelThreshold(double threshold) {
        this.delete_channel_threshold = threshold;
    }

    public double getDeleteMessageThreshold() {
        return this.delete_message_threshold;
    }

    public void setDeleteMessageThreshold(double threshold) {
        this.delete_message_threshold = threshold;
    }

    public double getDeleteUserThreshold() {
        return this.delete_user_threshold;
    }

    public void setDeleteUserThreshold(double threshold) {
        this.delete_user_threshold = threshold;
    }

    public double getEditMessageThreshold() {
        return this.edit_message_threshold;
    }

    public void setEditMessageThreshold(double threshold) {
        this.edit_message_threshold = threshold;
    }

    public double getInviteUserThreshold() {
        return this.invite_user_threshold;
    }

    public void setInviteUserThreshold(double threshold) {
        this.invite_user_threshold = threshold;
    }

    public double getPinMessageThreshold() {
        return this.pin_message_threshold;
    }

    public void setPinMessageThreshold(double threshold) {
        this.pin_message_threshold = threshold;
    }

    public double getPunishUserThreshold() {
        return this.punish_user_threshold;
    }

    public void setPunishUserThreshold(double threshold) {
        this.punish_user_threshold = threshold;
    }

    public double getUnpinMessageThreshold() {
        return this.unpin_message_threshold;
    }

    public void setUnpinMessageThreshold(double threshold) {
        this.unpin_message_threshold = threshold;
    }
}