package org.nlpcn.es4sql;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.metrics.avg.Avg;
import org.elasticsearch.search.aggregations.metrics.max.Max;
import org.elasticsearch.search.aggregations.metrics.min.Min;
import org.elasticsearch.search.aggregations.metrics.sum.Sum;
import org.elasticsearch.search.aggregations.metrics.valuecount.ValueCount;
import org.junit.Assert;
import org.junit.Test;
import org.nlpcn.es4sql.exception.SqlParseException;
import java.io.IOException;
import static org.nlpcn.es4sql.TestsConstants.TEST_INDEX;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


public class AggregationTest {

	private SearchDao searchDao = new SearchDao();

	@Test
	public void countSearch() throws IOException, SqlParseException{
		Aggregations result = query(String.format("SELECT COUNT(*) FROM %s/account", TEST_INDEX));
		ValueCount count = result.get("COUNT(*)");
		Assert.assertEquals(1000, count.getValue());
	}


	@Test
	public void sumTest() throws IOException, SqlParseException {
		Aggregations result = query(String.format("SELECT SUM(balance) FROM %s/account", TEST_INDEX));
		Sum sum = result.get("SUM(balance)");
		assertThat(sum.getValue(), equalTo(25714837.0));
	}

	@Test
	public void minTest() throws IOException, SqlParseException {
		Aggregations result = query(String.format("SELECT MIN(age) FROM %s/account", TEST_INDEX));
		Min min = result.get("MIN(age)");
		assertThat(min.getValue(), equalTo(20.0));
	}

	@Test
	public void maxTest() throws IOException, SqlParseException {
		Aggregations result = query(String.format("SELECT MAX(age) FROM %s/account", TEST_INDEX));
		Max max = result.get("MAX(age)");
		assertThat(max.getValue(), equalTo(40.0));
	}

	@Test
	public void avgTest() throws IOException, SqlParseException {
		Aggregations result = query(String.format("SELECT AVG(age) FROM %s/account", TEST_INDEX));
		Avg avg = result.get("AVG(age)");
		assertThat(avg.getValue(), equalTo(30.171));
	}


	@Test
	public void aliasTest() throws IOException, SqlParseException{
		Aggregations result = query(String.format("SELECT COUNT(*) AS mycount FROM %s/account", TEST_INDEX));
		assertThat(result.asMap(), hasKey("mycount"));
	}


	@Test
	public void sumDistinctOrderTest() throws IOException, SqlParseException {
		SearchRequestBuilder select = searchDao.explan("select sum(age),count(*), count(distinct age) from bank  group by gender order by count(distinct age)  desc  limit 3");
		System.out.println(select);
	}

	@Test
	public void sumSortAliasCount() throws IOException, SqlParseException {
		SearchRequestBuilder select = searchDao.explan("select sum(age),count(*) as kk, count(age) as k from bank  group by gender order by kk asc limit 10 ");
		System.out.println(select);
	}

	@Test
	public void sumSortCount() throws IOException, SqlParseException {
		SearchRequestBuilder select = searchDao.explan("select sum(age), count(age)  from bank  group by gender order by count(age) asc limit 2 ");
		System.out.println(select);
	}


	@Test
	public void countGroupByTest() throws IOException, SqlParseException {
		SearchRequestBuilder result = searchDao.explan("select count(*) from bank  group by gender ");
		System.out.println(result);
	}

	/**
	 * 区段group 聚合
	 * 
	 * http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/search-aggregations-bucket-range-aggregation.html
	 * 
	 * @throws IOException
	 * @throws SqlParseException
	 */
	@Test
	public void countGroupByRange() throws IOException, SqlParseException {
		SearchRequestBuilder result = searchDao.explan("select count(age) from bank  group by range(age, 20,25,30,35,40) ");
		System.out.println(result);
	}

	/**
	 * 时间 聚合 , 每天按照天聚合 参数说明:
	 * 
	 * <a>http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/search-aggregations-bucket-datehistogram-aggregation.html</a>
	 * 
	 * @throws IOException
	 * @throws SqlParseException
	 */
	@Test
	public void countGroupByDateTest() throws IOException, SqlParseException {
		SearchRequestBuilder result = searchDao.explan("select insert_time from online  group by date_histogram(field='insert_time','interval'='1.5h','format'='yyyy-MM') ");
		System.out.println(result);
	}

	/**
	 * 时间范围聚合
	 * 
	 * <a>http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/search-aggregations-bucket-daterange-aggregation.html</a>
	 * 
	 * @throws IOException
	 * @throws SqlParseException
	 */
	@Test
	public void countDateRangeTest() throws IOException, SqlParseException {
		SearchRequestBuilder result = searchDao
				.explan("select online from online  group by date_range(field='insert_time','format'='yyyy-MM-dd' ,'2014-08-18','2014-08-17','now-8d','now-7d','now-6d','now') ");
		System.out.println(result);
	}

	/**
	 * 时间范围聚合
	 * 
	 * <a>http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/search-aggregations-bucket-daterange-aggregation.html</a>
	 * 
	 * @throws IOException
	 * @throws SqlParseException
	 */
	@Test
	public void countTest() throws IOException, SqlParseException {
		SearchRequestBuilder result = searchDao
				.explan("select count(*),sum(all_tv_clinet) from online group by date_range(field='insert_time','format'='yyyy-MM-dd' ,'2014-08-18','2014-08-17','now-8d','now-7d','now-6d','now') ");
		System.out.println(result);
	}


	/**
	 * tophits 查询
	 * 
	 * <a>http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/search-aggregations-metrics-top-hits-aggregation.html</a>
	 * 
	 * @throws IOException
	 * @throws SqlParseException
	 */
	@Test
	public void topHitTest() throws IOException, SqlParseException {
		SearchRequestBuilder result = searchDao.explan("select topHits('size'=3,age='desc') from bank  group by gender ");
		System.out.println(result);
	}

	private Aggregations query(String query) throws SqlParseException {
		SearchDao searchDao = MainTestSuite.getSearchDao();
		SearchRequestBuilder select = searchDao.explan(query);
		return select.get().getAggregations();
	}

}
