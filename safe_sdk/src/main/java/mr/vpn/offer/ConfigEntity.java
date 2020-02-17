package mr.vpn.offer;

public class ConfigEntity {

    private String installUrl;
    private String installationPackage;
    private String schemeMask;
    private String[] verificationPackages;

    public String getInstallUrl() {
        return installUrl;
    }

    public void setInstallUrl(String installUrl) {
        this.installUrl = installUrl;
    }

    public String getInstallationPackage() {
        return installationPackage;
    }

    public void setInstallationPackage(String installationPackage) {
        this.installationPackage = installationPackage;
    }

    public String getSchemeMask() {
        return schemeMask;
    }

    public void setSchemeMask(String schemeMask) {
        this.schemeMask = schemeMask;
    }

    public String[] getVerificationPackages() {
        return verificationPackages;
    }

    public void setVerificationPackages(String[] verificationPackages) {
        this.verificationPackages = verificationPackages;
    }
}
