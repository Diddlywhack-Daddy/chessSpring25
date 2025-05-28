package service;

import dataaccess.DataAccess;
import model.result.ClearResult;


public class ClearService implements service.interfaces.ClearService {
    private final DataAccess data;

    public ClearService(DataAccess dataAccess){
        this.data = dataAccess;
    }

    @Override
    public ClearResult clear() {
        try{
            data.clear();
            return new ClearResult(true, "Successfully cleared data");
        } catch (Exception e) {
            return new ClearResult(false, "Error: "+e.getMessage());
        }
    }
}


