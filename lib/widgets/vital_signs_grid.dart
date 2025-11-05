import 'package:clinalert/widgets/patient_vitals_card.dart';
import 'package:flutter/material.dart';
import 'package:clinalert/models/vital_sign_model.dart';

class VitalSignsGrid extends StatelessWidget {
  final List<VitalSign> vitalSigns;

  const VitalSignsGrid({super.key, required this.vitalSigns});

  @override
  Widget build(BuildContext context) {
    return GridView.builder(
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 2,
        crossAxisSpacing: 12,
        mainAxisSpacing: 12,
        childAspectRatio: 1.5,
      ),
      itemCount: vitalSigns.length,
      itemBuilder: (context, index) {
        return PatientVitalsCard(vitalSign: vitalSigns[index]);
      },
    );
  }
}