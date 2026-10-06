/** The outcome of FinancialAnalyzer.analyze(). Immutable. */
public final class AnalysisResult {

    private final boolean valid;
    private final String message;           // why the result is empty (only when !valid)
    private final double totalDirectCost;
    private final double totalIndirectCost; // treated as the fixed costs
    private final double unitCost;
    private final double unitVariableCost;  // direct cost / production quantity
    private final double recommendedPrice;
    private final double breakEvenPoint;
    private final double contributionMargin;
    private final double contributionMarginRatio; // 0.25 means 25%
    private final double operatingLeverage;
    private final double profitAtExpectedSales;

    public AnalysisResult(double totalDirectCost, double totalIndirectCost, double unitCost,
                          double unitVariableCost, double recommendedPrice, double breakEvenPoint,
                          double contributionMargin, double contributionMarginRatio,
                          double operatingLeverage, double profitAtExpectedSales) {
        this.valid = true;
        this.message = "";
        this.totalDirectCost = totalDirectCost;
        this.totalIndirectCost = totalIndirectCost;
        this.unitCost = unitCost;
        this.unitVariableCost = unitVariableCost;
        this.recommendedPrice = recommendedPrice;
        this.breakEvenPoint = breakEvenPoint;
        this.contributionMargin = contributionMargin;
        this.contributionMarginRatio = contributionMarginRatio;
        this.operatingLeverage = operatingLeverage;
        this.profitAtExpectedSales = profitAtExpectedSales;
    }

    private AnalysisResult(String message) {
        this.valid = false;
        this.message = message;
        this.totalDirectCost = 0;
        this.totalIndirectCost = 0;
        this.unitCost = 0;
        this.unitVariableCost = 0;
        this.recommendedPrice = 0;
        this.breakEvenPoint = 0;
        this.contributionMargin = 0;
        this.contributionMarginRatio = 0;
        this.operatingLeverage = 0;
        this.profitAtExpectedSales = 0;
    }

    /** An all-zero result, shown when the numbers can't be analysed yet. */
    public static AnalysisResult empty(String reason) {
        return new AnalysisResult(reason);
    }

    public boolean isValid()                   { return valid; }
    public String getMessage()                 { return message; }
    public double getTotalDirectCost()         { return totalDirectCost; }
    public double getTotalIndirectCost()       { return totalIndirectCost; }
    public double getUnitCost()                { return unitCost; }
    public double getUnitVariableCost()        { return unitVariableCost; }
    public double getRecommendedPrice()        { return recommendedPrice; }
    public double getBreakEvenPoint()          { return breakEvenPoint; }
    public double getContributionMargin()      { return contributionMargin; }
    public double getContributionMarginRatio() { return contributionMarginRatio; }
    public double getOperatingLeverage()       { return operatingLeverage; }
    public double getProfitAtExpectedSales()   { return profitAtExpectedSales; }
}
