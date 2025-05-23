package service;

import dataaccess.DataAccess;
import model.BasicResult;

public class ClearService implements service.interfaces.ClearService {
    private final DataAccess data;

    public ClearService(DataAccess dataAccess){
        this.data = dataAccess;
    }

    @Override
    public BasicResult clear() {
        try{
            data.clear();
            return new BasicResult(true, null);
        } catch (Exception e) {
            return new BasicResult(false, "Error: "+e.getMessage());
        }
    }
}


