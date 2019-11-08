package dev.xframe.test.game;

import org.junit.Assert;

import dev.xframe.inject.Inject;
import dev.xframe.inject.Loadable;
import dev.xframe.inject.Repository;


@Repository
public class TRepository extends TAbsRepo implements Loadable {

    @Inject
    private TestExecution testExecution;
    
    @Override
    public void load() {
        Assert.assertNotNull(jdbcTemplate);
        testExecution.executing(TRepository.class);
    }

    public void save() {
        testExecution.executing(TRepository.class);
    }

}
