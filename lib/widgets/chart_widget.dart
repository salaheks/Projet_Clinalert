import 'package:flutter/material.dart';

// Lightweight placeholder chart widget with animated bars/line.
class ChartWidget extends StatelessWidget {
  final List<double> data;
  final Color color;
  final String title;
  final bool showLine;

  const ChartWidget({super.key, required this.data, required this.color, required this.title, this.showLine = false});

  @override
  Widget build(BuildContext context) {
    final maxValue = (data.isEmpty ? 1.0 : data.reduce((a, b) => a > b ? a : b)) * 1.2;
    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        boxShadow: [BoxShadow(color: Colors.black.withOpacity(0.04), blurRadius: 12, offset: const Offset(0, 6))],
      ),
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(title, style: const TextStyle(fontWeight: FontWeight.w600)),
          const SizedBox(height: 12),
          SizedBox(
            height: 140,
            child: LayoutBuilder(
              builder: (context, constraints) {
                final barWidth = constraints.maxWidth / (data.length * 2 + 1);
                return Stack(
                  children: [
                    Row(
                      crossAxisAlignment: CrossAxisAlignment.end,
                      children: [
                        for (int i = 0; i < data.length; i++)
                          Expanded(
                            flex: 2,
                            child: Padding(
                              padding: EdgeInsets.only(left: i == 0 ? 0 : barWidth / 2, right: barWidth / 2),
                              child: TweenAnimationBuilder<double>(
                                tween: Tween(begin: 0, end: data[i] / maxValue),
                                duration: const Duration(milliseconds: 500),
                                curve: Curves.easeOutCubic,
                                builder: (context, t, _) {
                                  return Container(
                                    height: constraints.maxHeight * t,
                                    decoration: BoxDecoration(
                                      color: color.withOpacity(0.8),
                                      borderRadius: BorderRadius.circular(8),
                                    ),
                                  );
                                },
                              ),
                            ),
                          ),
                      ],
                    ),
                  ],
                );
              },
            ),
          ),
        ],
      ),
    );
  }
}


