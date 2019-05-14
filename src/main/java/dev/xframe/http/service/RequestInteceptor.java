package dev.xframe.http.service;

import dev.xframe.injection.Combine;

@Combine
public interface RequestInteceptor {
    
    Response intecept(Request req);

}
