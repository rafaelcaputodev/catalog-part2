package com.caputo.dscatalog.dto;

import com.caputo.dscatalog.services.validations.UserInsertValid;

@UserInsertValid
public class UserInsertDTO extends UserDTO{
    public static final long serialVersionUID = 1L;

    private String password;

    public UserInsertDTO() {
        super();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
