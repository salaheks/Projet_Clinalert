import 'package:fl_chart/fl_chart.dart';
import 'package:flutter/material.dart';

// Lightweight placeholder chart widget with animated bars/line.
class ChartWidget extends StatelessWidget {
  final List<double> data;
  final Color color;
  final String title;
  final bool showLine;

  const ChartWidget({
    super.key,
    required this.data,
    required this.color,
    required this.title,
    this.showLine = false,
  });

  @override
  Widget build(BuildContext context) {
    // Calculate max value for y-axis
    final maxValue = (data.isEmpty ? 1.0 : data.reduce((a, b) => a > b ? a : b)) * 1.2;

    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withAlpha(10),
            blurRadius: 12,
            offset: const Offset(0, 6),
          )
        ],
      ),
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          if (title.isNotEmpty)
            Text(title, style: const TextStyle(fontWeight: FontWeight.w600)),
          if (title.isNotEmpty) const SizedBox(height: 8),
          Expanded(
            child: BarChart(
              BarChartData(
                alignment: BarChartAlignment.spaceAround,
                maxY: maxValue,
                barTouchData: BarTouchData(
                  enabled: true,
                  touchTooltipData: BarTouchTooltipData(
                    getTooltipColor: (_) => Colors.blueGrey,
                    tooltipPadding: const EdgeInsets.all(8),
                    tooltipMargin: 8,
                    getTooltipItem: (group, groupIndex, rod, rodIndex) {
                      return BarTooltipItem(
                        rod.toY.round().toString(),
                        const TextStyle(
                          color: Colors.white,
                          fontWeight: FontWeight.bold,
                        ),
                      );
                    },
                  ),
                ),
                titlesData: FlTitlesData(
                  show: true,
                  bottomTitles: AxisTitles(
                    sideTitles: SideTitles(
                      showTitles: true,
                      getTitlesWidget: (value, meta) {
                        return const Padding(
                          padding: EdgeInsets.only(top: 8.0),
                          child: Text(
                            '', // You can add labels here if needed based on index
                            style: TextStyle(
                              color: Colors.grey,
                              fontWeight: FontWeight.bold,
                              fontSize: 10,
                            ),
                          ),
                        );
                      },
                      reservedSize: 20,
                    ),
                  ),
                  leftTitles: const AxisTitles(
                    sideTitles: SideTitles(showTitles: false),
                  ),
                  topTitles: const AxisTitles(
                    sideTitles: SideTitles(showTitles: false),
                  ),
                  rightTitles: const AxisTitles(
                    sideTitles: SideTitles(showTitles: false),
                  ),
                ),
                gridData: const FlGridData(show: false),
                borderData: FlBorderData(show: false),
                barGroups: data.asMap().entries.map((entry) {
                  return BarChartGroupData(
                    x: entry.key,
                    barRods: [
                      BarChartRodData(
                        toY: entry.value,
                        color: color.withAlpha(204),
                        width: 12, // Fixed width for bars
                        borderRadius: const BorderRadius.vertical(
                          top: Radius.circular(4),
                        ),
                        backDrawRodData: BackgroundBarChartRodData(
                          show: true,
                          toY: maxValue,
                          color: color.withAlpha(26),
                        ),
                      ),
                    ],
                  );
                }).toList(),
              ),
            ),
          ),
        ],
      ),
    );
  }
}


