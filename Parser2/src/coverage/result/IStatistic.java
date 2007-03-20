package coverage.result;

import java.util.List;

/**
 * Repr�sentiert Statistik, die die Anzahl der getesteten und gesamten
 * Codest�cke beinhalten. Eine Statistik kann sich aus mehreren Statistiken
 * zusammensetzten.
 * 
 * @author Alex Salnikow
 * 
 */

public interface IStatistic {

	public int getTotalNumber();

	public int getTestedNumber();

	public List<IStatistic> getSubStatistics();

	public void addSubStatistik(IStatistic statistic);

	public String getName();

}
