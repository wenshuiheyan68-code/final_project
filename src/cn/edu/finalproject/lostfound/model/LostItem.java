package cn.edu.finalproject.lostfound.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 校园失物招领记录。
 * 使用 Date 保存发布时间，并实现 Serializable，便于通过 Socket 传输和本地快照持久化。
 */
public class LostItem implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final long id;
    private final ItemType type;
    private final String title;
    private final String description;
    private final String location;
    private final String contact;
    private final Date publishTime;
    private ItemStatus status;
    private String claimant;
    private Date claimTime;

    public LostItem(long id, ItemType type, String title, String description, String location, String contact) {
        this.id = id;
        this.type = type;
        this.title = clean(title, "物品名称");
        this.description = description == null ? "" : description.trim();
        this.location = clean(location, "地点");
        this.contact = clean(contact, "联系方式");
        this.publishTime = new Date();
        this.status = ItemStatus.OPEN;
    }

    private static String clean(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + "不能为空");
        }
        return value.trim();
    }

    public synchronized boolean claim(String claimer) {
        if (status == ItemStatus.CLAIMED) {
            return false;
        }
        this.claimant = clean(claimer, "认领人");
        this.claimTime = new Date();
        this.status = ItemStatus.CLAIMED;
        return true;
    }

    public boolean matches(String keyword, String typeValue, String statusValue) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase();
        boolean keywordMatched = normalizedKeyword.isEmpty()
                || title.toLowerCase().contains(normalizedKeyword)
                || description.toLowerCase().contains(normalizedKeyword)
                || location.toLowerCase().contains(normalizedKeyword);
        boolean typeMatched = typeValue == null || typeValue.trim().isEmpty()
                || type.name().equalsIgnoreCase(typeValue.trim());
        boolean statusMatched = statusValue == null || statusValue.trim().isEmpty()
                || status.name().equalsIgnoreCase(statusValue.trim());
        return keywordMatched && typeMatched && statusMatched;
    }

    public long getId() {
        return id;
    }

    public ItemType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public String getContact() {
        return contact;
    }

    public Date getPublishTime() {
        return new Date(publishTime.getTime());
    }

    public ItemStatus getStatus() {
        return status;
    }

    public String getClaimant() {
        return claimant;
    }

    public Date getClaimTime() {
        return claimTime == null ? null : new Date(claimTime.getTime());
    }

    public String toDisplayText() {
        String shortDescription = description.length() <= 48
                ? description : description.substring(0, Math.max(0, 45)) + "...";
        String claimedText = status == ItemStatus.CLAIMED
                ? "，认领人：" + claimant + "，认领时间：" + DATE_FORMAT.format(claimTime) : "";
        return String.format("[%d] %s | %s | %s | 地点：%s | 联系：%s | 发布时间：%s%s%n  描述：%s",
                id, type.getDisplayName(), status.getDisplayName(), title, location, contact,
                DATE_FORMAT.format(publishTime), claimedText, shortDescription);
    }
}
