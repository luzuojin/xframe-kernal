package dev.xframe.test.game;

import org.junit.Assert;

import dev.xframe.injection.Inject;
import dev.xframe.injection.Loadable;
import dev.xframe.injection.Repository;


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
