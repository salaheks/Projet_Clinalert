import 'package:flutter/material.dart';
import 'chart_widget.dart';

// HealthDataChart composes ChartWidget and allows switching timeframe.
class HealthDataChart extends StatefulWidget {
  final String title;
  final List<double> day;
  final List<double> week;
  final List<double> month;
  final Color color;

  const HealthDataChart({super.key, required this.title, required this.day, required this.week, required this.month, required this.color});

  @override
  State<HealthDataChart> createState() => _HealthDataChartState();
}

class _HealthDataChartState extends State<HealthDataChart> {
  String _period = 'Week';

  @override
  Widget build(BuildContext context) {
    List<double> data;
    switch (_period) {
      case 'Day':
        data = widget.day;
        break;
      case 'Month':
        data = widget.month;
        break;
      case 'Week':
      default:
        data = widget.week;
    }

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text(widget.title, style: const TextStyle(fontWeight: FontWeight.w600)),
            SegmentedButton<String>(
              segments: const [
                ButtonSegment(value: 'Day', label: Text('Day')),
                ButtonSegment(value: 'Week', label: Text('Week')),
                ButtonSegment(value: 'Month', label: Text('Month')),
              ],
              selected: {_period},
              onSelectionChanged: (s) => setState(() => _period = s.first),
            ),
          ],
        ),
        const SizedBox(height: 12),
        ChartWidget(data: data, color: widget.color, title: widget.title),
      ],
    );
  }
}


