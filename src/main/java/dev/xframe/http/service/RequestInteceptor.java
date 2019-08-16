package dev.xframe.http.service;

import dev.xframe.injection.Synthetic;

@Synthetic
public interface RequestInteceptor {
    
    Response intecept(Request req);

}
