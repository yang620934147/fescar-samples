package com.alibaba.fescar.samples.tcc.starter;

import com.alibaba.fescar.common.util.StringUtils;
import com.alibaba.fescar.samples.tcc.ApplicationKeeper;
import com.alibaba.fescar.samples.tcc.action.ResultHolder;
import com.alibaba.fescar.samples.tcc.action.impl.TccActionOneImpl;
import com.alibaba.fescar.samples.tcc.action.impl.TccActionTwoImpl;
import com.alibaba.fescar.samples.tcc.service.TccTransactionService;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author zhangsen
 */
public class LocalTccTransactionStarter {

    static AbstractApplicationContext applicationContext = null;

    static TccTransactionService tccTransactionService = null;

    static TccActionOneImpl tccActionOne = null;

    static TccActionTwoImpl tccActionTwo = null;

    public static void main(String[] args) throws InterruptedException {
        applicationContext = new ClassPathXmlApplicationContext(new String[] {"spring/fescar-tcc.xml"});

        tccTransactionService = (TccTransactionService) applicationContext.getBean("tccTransactionService"   );

        tccActionOne = (TccActionOneImpl) applicationContext.getBean("tccActionOne");
        tccActionTwo = (TccActionTwoImpl) applicationContext.getBean("tccActionTwo");

        //分布式事务提交demo
        transactionCommitDemo();

        //分布式事务回滚demo
        transactionRollbackDemo();

        new ApplicationKeeper(applicationContext).keep();
    }

    private static void transactionCommitDemo() throws InterruptedException {
        String txId = tccTransactionService.doTransactionCommit();
        System.out.println(txId);
        Assert.isTrue(StringUtils.isNotBlank(txId), "事务开启失败");

        Thread.sleep(1000L);

        Assert.isTrue("T".equals(ResultHolder.getActionOneResult(txId)), "tccActionOne commit failed");
        Assert.isTrue("T".equals(ResultHolder.getActionTwoResult(txId)), "tccActionTwo commit failed");

        System.out.println("transaction commit demo finish.");
    }

    private static void transactionRollbackDemo() throws InterruptedException {
        Map map = new HashMap();
        try{
            tccTransactionService.doTransactionRollback(map);
            Assert.isTrue(false, "分布式事务未回滚");
        }catch (Throwable t) {
            Assert.isTrue(true, "分布式事务异常回滚");
        }
        String txId = (String) map.get("xid");
        Thread.sleep(1000L);

        Assert.isTrue("R".equals(ResultHolder.getActionOneResult(txId)), "tccActionOne commit failed");
        Assert.isTrue("R".equals(ResultHolder.getActionTwoResult(txId)), "tccActionTwo commit failed");

        System.out.println("transaction rollback demo finish.");
    }

}