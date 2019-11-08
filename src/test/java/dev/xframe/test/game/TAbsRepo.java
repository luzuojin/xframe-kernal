package dev.xframe.test.game;

import dev.xframe.inject.Inject;

public abstract class TAbsRepo {

    @Inject
    protected TJdbcTemplate jdbcTemplate;
    
}
