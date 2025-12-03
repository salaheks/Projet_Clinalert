// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'consent.dart';

// **************************************************************************
// TypeAdapterGenerator
// **************************************************************************

class ConsentAdapter extends TypeAdapter<Consent> {
  @override
  final int typeId = 1;

  @override
  Consent read(BinaryReader reader) {
    final numOfFields = reader.readByte();
    final fields = <int, dynamic>{
      for (int i = 0; i < numOfFields; i++) reader.readByte(): reader.read(),
    };
    return Consent(
      id: fields[0] as String,
      patientId: fields[1] as String,
      timestamp: fields[2] as DateTime,
      granted: fields[3] as bool,
      permissions: (fields[4] as List).cast<String>(),
    );
  }

  @override
  void write(BinaryWriter writer, Consent obj) {
    writer
      ..writeByte(5)
      ..writeByte(0)
      ..write(obj.id)
      ..writeByte(1)
      ..write(obj.patientId)
      ..writeByte(2)
      ..write(obj.timestamp)
      ..writeByte(3)
      ..write(obj.granted)
      ..writeByte(4)
      ..write(obj.permissions);
  }

  @override
  int get hashCode => typeId.hashCode;

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is ConsentAdapter &&
          runtimeType == other.runtimeType &&
          typeId == other.typeId;
}
