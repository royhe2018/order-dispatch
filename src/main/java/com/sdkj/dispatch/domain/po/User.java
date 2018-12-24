package com.sdkj.dispatch.domain.po;

public class User {
    private Long id;

    private String account;

    private String headImg;

    private String nickName;

    private Integer sex;

    private Integer userType;

    private Long refereeId;

    private String registrionId;
    
    private String mapTerminalId;
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account == null ? null : account.trim();
    }

    public String getHeadImg() {
        return headImg;
    }

    public void setHeadImg(String headImg) {
        this.headImg = headImg == null ? null : headImg.trim();
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName == null ? null : nickName.trim();
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public Integer getUserType() {
        return userType;
    }

    public void setUserType(Integer userType) {
        this.userType = userType;
    }

    public Long getRefereeId() {
        return refereeId;
    }

    public void setRefereeId(Long refereeId) {
        this.refereeId = refereeId;
    }

	public String getRegistrionId() {
		return registrionId;
	}

	public void setRegistrionId(String registrionId) {
		this.registrionId = registrionId;
	}

	public String getMapTerminalId() {
		return mapTerminalId;
	}

	public void setMapTerminalId(String mapTerminalId) {
		this.mapTerminalId = mapTerminalId;
	}
	
}