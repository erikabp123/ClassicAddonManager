package com.CAM.AddonManagement;

public interface UpdateProgressListener {

    public void informStart(int position);

    public void informFinish(int position, int statusCode);

}
