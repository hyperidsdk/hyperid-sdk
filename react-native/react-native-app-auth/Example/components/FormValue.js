import React from 'react';
import { Text, StyleSheet } from 'react-native';

const FormValue = props => (
  <Text
    style={styles.text}
    numberOfLines={2}
    ellipsizeMode="tail"
    {...props}
  />
);

const styles = StyleSheet.create({
  text: {
    fontSize: 14,
    backgroundColor: 'transparent',
    marginBottom: 20,
  }
});

export default FormValue;
