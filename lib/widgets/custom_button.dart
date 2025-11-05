import 'package:flutter/material.dart';
import '../themes/app_theme.dart';

class CustomButton extends StatelessWidget {
  final String text;
  final VoidCallback onPressed;
  final bool isLoading;
  final bool isOutlined;
  final bool isTextButton;
  final IconData? icon;
  final double? width;
  final double? height;
  final Color? backgroundColor;
  final Color? textColor;
  final double borderRadius;
  final EdgeInsetsGeometry padding;

  const CustomButton({
    super.key,
    required this.text,
    required this.onPressed,
    this.isLoading = false,
    this.isOutlined = false,
    this.isTextButton = false,
    this.icon,
    this.width,
    this.height,
    this.backgroundColor,
    this.textColor,
    this.borderRadius = 12,
    this.padding = const EdgeInsets.symmetric(horizontal: 24, vertical: 16),
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    
    Widget buttonChild;
    
    if (isLoading) {
      buttonChild = const SizedBox(
        width: 20,
        height: 20,
        child: CircularProgressIndicator(
          strokeWidth: 2,
          valueColor: AlwaysStoppedAnimation<Color>(Colors.white),
        ),
      );
    } else {
      final textWidget = Text(
        text,
        style: theme.textTheme.bodyLarge?.copyWith(
          color: textColor ?? (isOutlined || isTextButton
              ? backgroundColor ?? AppThemes.primaryBlue
              : Colors.white),
          fontWeight: FontWeight.w600,
        ),
      );
      
      if (icon != null) {
        buttonChild = Row(
          mainAxisSize: MainAxisSize.min,
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              icon,
              size: 18,
              color: textColor ?? (isOutlined || isTextButton
                  ? backgroundColor ?? AppThemes.primaryBlue
                  : Colors.white),
            ),
            const SizedBox(width: 8),
            textWidget,
          ],
        );
      } else {
        buttonChild = textWidget;
      }
    }

    final buttonStyle = isOutlined
        ? OutlinedButton.styleFrom(
            side: BorderSide(
              color: backgroundColor ?? AppThemes.primaryBlue,
              width: 2,
            ),
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(borderRadius),
            ),
            padding: padding,
            minimumSize: Size(width ?? double.infinity, height ?? 48),
          )
        : isTextButton
            ? TextButton.styleFrom(
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(borderRadius),
                ),
                padding: padding,
                minimumSize: Size(width ?? double.infinity, height ?? 48),
              )
            : ElevatedButton.styleFrom(
                backgroundColor: backgroundColor ?? AppThemes.primaryBlue,
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(borderRadius),
                ),
                padding: padding,
                minimumSize: Size(width ?? double.infinity, height ?? 48),
                elevation: 2,
                shadowColor: (backgroundColor ?? AppThemes.primaryBlue)
                    .withOpacity(0.3),
              );

    if (isOutlined) {
      return OutlinedButton(
        onPressed: isLoading ? null : onPressed,
        style: buttonStyle as ButtonStyle,
        child: buttonChild,
      );
    } else if (isTextButton) {
      return TextButton(
        onPressed: isLoading ? null : onPressed,
        style: buttonStyle as ButtonStyle,
        child: buttonChild,
      );
    } else {
      return ElevatedButton(
        onPressed: isLoading ? null : onPressed,
        style: buttonStyle as ButtonStyle,
        child: buttonChild,
      );
    }
  }
}