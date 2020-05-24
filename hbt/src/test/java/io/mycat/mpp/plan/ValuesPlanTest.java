package io.mycat.mpp.plan;

import io.mycat.mpp.DataContext;
import io.mycat.mpp.MyRelBuilder;
import io.mycat.mpp.SqlValue;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ValuesPlanTest {
   final DataContext dataContext = DataContext.DEFAULT;

    @Test
    public void test(){

        ValuesPlan valuesPlan = ValuesPlan.create(
                Type.of(
                        Column.of("id", Integer.class),
                        Column.of("name", String.class)
                        ),
                values(new Object[]{1,"1"}, new Object[]{2,"2"})
                );
        Type columns = valuesPlan.getColumns();
        Scanner scan = valuesPlan.scan(dataContext, 0);
        String collect = scan.stream().map(i -> i.toString())
                .collect(Collectors.joining());

        OrderPlan orderPlan = OrderPlan.create(valuesPlan, new int[]{1}, new boolean[]{false});
        String collect1 = orderPlan.scan(dataContext, 0).stream().map(i -> i.toString()).collect(Collectors.joining());

        LimitPlan limitPlan =  LimitPlan.create(orderPlan,0,1);

        Scanner scan1 = limitPlan.scan(dataContext, 0);
        String collect2 = scan1.stream().map(i -> i.toString()).collect(Collectors.joining());

        AggregationPlan aggregationPlan = AggregationPlan.create(limitPlan, new String[]{"count","avg"}, Type.of(
                Column.of("count()", Long.class),
                Column.of("avg(id)", Double.class)
                ),
                Collections.singletonList( Collections.singletonList(1)), new int[]{});

        String collect3 = aggregationPlan.scan(dataContext, 0).stream().map(i -> i.toString()).collect(Collectors.joining());


    }
    @Test
    public void test2(){

        ValuesPlan one = ValuesPlan.create(
                Type.of(
                        Column.of("id", Integer.class),
                        Column.of("name", String.class)
                ),
                values(new Object[]{1,"1"}, new Object[]{2,"2"})
        );
        ValuesPlan two = ValuesPlan.create(
                Type.of(
                        Column.of("id", Integer.class),
                        Column.of("name", String.class)
                ),
                values(new Object[]{1,"1"}, new Object[]{2,"2"})
        );

        UnionPlan unionPlan = UnionPlan.create(Arrays.asList(one, two));
        Scanner scan = unionPlan.scan(dataContext, 0);
        String collect = scan.stream().map(i -> i.toString()).collect(Collectors.joining());
    }

    @Test
    public void test3(){
        MyRelBuilder builder = new MyRelBuilder();

        ValuesPlan one = ValuesPlan.create(
                Type.of(
                        Column.of("id", Integer.class),
                        Column.of("name", String.class)
                ),
                values(new Object[]{1,"1"}, new Object[]{2,"2"})
        );
        builder.push(one);
        SqlValue id = builder.field("id");
        SqlValue equality = builder.equality(id, builder.literal(1));

        FilterPlan queryPlan = FilterPlan.create(one, equality);
        Scanner scan = queryPlan.scan(dataContext, 0);
        String collect = scan.stream().map(i -> i.toString()).collect(Collectors.joining());
    }
    private List<Object[]> values(Object[]... objects) {
        return Arrays.asList(objects);
    }

}