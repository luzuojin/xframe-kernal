package dev.xframe.http.service.config;

import dev.xframe.http.service.Request;
import dev.xframe.http.service.Response;
import dev.xframe.injection.Synthetic;

@Synthetic
public interface RequestInteceptor {
    
    Response intecept(Request req);

}
