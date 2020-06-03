package dev.xframe.test.game;

import dev.xframe.inject.Inject;
import dev.xframe.inject.Repository;

@Repository
public abstract class TAbsRepo {

    @Inject
    protected TJdbcTemplate jdbcTemplate;
    
}
