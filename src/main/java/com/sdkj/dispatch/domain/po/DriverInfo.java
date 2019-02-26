package com.sdkj.dispatch.domain.po;

public class DriverInfo {
    private Long id;

    private Long userId;

    private String idCardNo;

    private Integer idCardType;

    private String drivingLicenseNo;

    private String drivingLicenseFileNo;

    private String drivingLicenseType;

    private String drivingLicenseImage;
    
    private String onDutyStatus;
    private Integer status;
    private String driverType;
    private Integer vehicleTypeId;
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getIdCardNo() {
        return idCardNo;
    }

    public void setIdCardNo(String idCardNo) {
        this.idCardNo = idCardNo == null ? null : idCardNo.trim();
    }

    public Integer getIdCardType() {
        return idCardType;
    }

    public void setIdCardType(Integer idCardType) {
        this.idCardType = idCardType;
    }

    public String getDrivingLicenseNo() {
        return drivingLicenseNo;
    }

    public void setDrivingLicenseNo(String drivingLicenseNo) {
        this.drivingLicenseNo = drivingLicenseNo == null ? null : drivingLicenseNo.trim();
    }

    public String getDrivingLicenseFileNo() {
        return drivingLicenseFileNo;
    }

    public void setDrivingLicenseFileNo(String drivingLicenseFileNo) {
        this.drivingLicenseFileNo = drivingLicenseFileNo == null ? null : drivingLicenseFileNo.trim();
    }

    public String getDrivingLicenseType() {
        return drivingLicenseType;
    }

    public void setDrivingLicenseType(String drivingLicenseType) {
        this.drivingLicenseType = drivingLicenseType == null ? null : drivingLicenseType.trim();
    }

    public String getDrivingLicenseImage() {
        return drivingLicenseImage;
    }

    public void setDrivingLicenseImage(String drivingLicenseImage) {
        this.drivingLicenseImage = drivingLicenseImage == null ? null : drivingLicenseImage.trim();
    }

	public String getOnDutyStatus() {
		return onDutyStatus;
	}

	public void setOnDutyStatus(String onDutyStatus) {
		this.onDutyStatus = onDutyStatus;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getDriverType() {
		return driverType;
	}

	public void setDriverType(String driverType) {
		this.driverType = driverType;
	}

	public Integer getVehicleTypeId() {
		return vehicleTypeId;
	}

	public void setVehicleTypeId(Integer vehicleTypeId) {
		this.vehicleTypeId = vehicleTypeId;
	}
}