package dev.xframe.test.game;

import dev.xframe.injection.Inject;

public abstract class TAbsRepo {

    @Inject
    protected TJdbcTemplate jdbcTemplate;
    
}
