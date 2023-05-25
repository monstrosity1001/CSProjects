#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "asserts.h"
// Necessary due to static functions in state.c
#include "state.c"

/* Look at asserts.c for some helpful assert functions */

int greater_than_forty_two(int x) {
  return x > 42;
}

bool is_vowel(char c) {
  char* vowels = "aeiouAEIOU";
  for (int i = 0; i < strlen(vowels); i++) {
    if (c == vowels[i]) {
      return true;
    }
  }
  return false;
}

/*
  Example 1: Returns true if all test cases pass. False otherwise.
    The function greater_than_forty_two(int x) will return true if x > 42. False otherwise.
    Note: This test is NOT comprehensive
*/
bool test_greater_than_forty_two() {
  int testcase_1 = 42;
  bool output_1 = greater_than_forty_two(testcase_1);
  if (!assert_false("output_1", output_1)) {
    return false;
  }

  int testcase_2 = -42;
  bool output_2 = greater_than_forty_two(testcase_2);
  if (!assert_false("output_2", output_2)) {
    return false;
  }

  int testcase_3 = 4242;
  bool output_3 = greater_than_forty_two(testcase_3);
  if (!assert_true("output_3", output_3)) {
    return false;
  }

  return true;
}

/*
  Example 2: Returns true if all test cases pass. False otherwise.
    The function is_vowel(char c) will return true if c is a vowel (i.e. c is a,e,i,o,u)
    and returns false otherwise
    Note: This test is NOT comprehensive
*/
bool test_is_vowel() {
  char testcase_1 = 'a';
  bool output_1 = is_vowel(testcase_1);
  if (!assert_true("output_1", output_1)) {
    return false;
  }

  char testcase_2 = 'e';
  bool output_2 = is_vowel(testcase_2);
  if (!assert_true("output_2", output_2)) {
    return false;
  }

  char testcase_3 = 'i';
  bool output_3 = is_vowel(testcase_3);
  if (!assert_true("output_3", output_3)) {
    return false;
  }

  char testcase_4 = 'o';
  bool output_4 = is_vowel(testcase_4);
  if (!assert_true("output_4", output_4)) {
    return false;
  }

  char testcase_5 = 'u';
  bool output_5 = is_vowel(testcase_5);
  if (!assert_true("output_5", output_5)) {
    return false;
  }

  char testcase_6 = 'k';
  bool output_6 = is_vowel(testcase_6);
  if (!assert_false("output_6", output_6)) {
    return false;
  }

  return true;
}

/* Task 4.1 */

bool test_is_tail() {
  // TODO: Implement this function.
  /* char testcase_1 = '#';
  bool output_1 = is_tail(testcase_1);
  if (!assert_false("output_1", output_1)) {
    return false;
  } */

  char testcase_2 = 'w';
  bool output_2 = is_tail(testcase_2);
  if (!assert_true("output_2", output_2)) {
    return false;
  }

  char testcase_3 = 'a';
  bool output_3 = is_tail(testcase_3);
  if (!assert_true("output_3", output_3)) {
    return false;
  }

  char testcase_4 = 's';
  bool output_4 = is_tail(testcase_4);
  if (!assert_true("output_4", output_4)) {
    return false;
  }

  char testcase_5 = 'd';
  bool output_5 = is_tail(testcase_5);
  if (!assert_true("output_5", output_5)) {
    return false;
  }

  /*char testcase_6 = ' ';
  bool output_6 = is_tail(testcase_6);
  if (!assert_false("output_6", output_6)) {
    return false;
  }*/

  return true;
}

bool test_is_head() {
  // TODO: Implement this function.
  /*char testcase_1 = 'w';
  bool output_1 = is_head(testcase_1);
  if (!assert_false("output_1", output_1)) {
    return false;
  } */

  char testcase_2 = 'W';
  bool output_2 = is_head(testcase_2);
  if (!assert_true("output_2", output_2)) {
    return false;
  }

  char testcase_3 = 'A';
  bool output_3 = is_head(testcase_3);
  if (!assert_true("output_3", output_3)) {
    return false;
  }

  char testcase_4 = 'S';
  bool output_4 = is_head(testcase_4);
  if (!assert_true("output_4", output_4)) {
    return false;
  }

  char testcase_5 = 'D';
  bool output_5 = is_head(testcase_5);
  if (!assert_true("output_5", output_5)) {
    return false;
  }

  /* char testcase_6 = '*';
  bool output_6 = is_head(testcase_6);
  if (!assert_false("output_6", output_6)) {
    return false;
  } */

  return true;
}

bool test_is_snake() {
  // TODO: Implement this function.
  char in_snake[] = "wasd^<v>WASDx";
  for (int i = 0; i < strlen(in_snake); i++) {
    char testcase = in_snake[i]; 
    bool output = is_snake(testcase); 
    if (!assert_true("output", output)) {
      return false; 
    }
  }

  /*char testcase_6 = '*';
  bool output_6 = is_snake(testcase_6);
  if (!assert_false("output_6", output_6)) {
    return false;
  }

  char testcase_7 = ' ';
  bool output_7 = is_snake(testcase_7);
  if (!assert_false("output_7", output_7)) {
    return false;
  }

  char testcase_8 = '#';
  bool output_8 = is_snake(testcase_8);
  if (!assert_false("output_8", output_8)) {
    return false;
  } */

  return true;
}

bool test_body_to_tail() {
  // TODO: Implement this function.
  char testcase_1 = '^';
  char output_1 = body_to_tail(testcase_1);
  bool val_1 = false;
  if (output_1 == 'w'){
    val_1 = true; 
  }
  if (!assert_true("output_1", val_1)) {
    return false;
  }

  char testcase_2 = '<';
  char output_2 = body_to_tail(testcase_2);
  bool val_2 = false;
  if (output_2 == 'a'){
    val_2 = true; 
  }
  if (!assert_true("output_2", val_2)) {
    return false;
  }

  char testcase_3 = 'v';
  char output_3 = body_to_tail(testcase_3);
  bool val_3 = false;
  if (output_3 == 's'){
    val_3 = true; 
  }
  if (!assert_true("output_3", val_3)) {
    return false;
  }

  char testcase_4 = '>';
  char output_4 = body_to_tail(testcase_4);
  bool val_4 = false;
  if (output_4 == 'd'){
    val_4 = true; 
  }
  if (!assert_true("output_4", val_4)) {
    return false;
  }

  /*char testcase_5 = '#';
  char output_5 = body_to_tail(testcase_5);
  bool val_5 = false;
  if (output_5 == '?'){
    val_5 = true; 
  }
  if (!assert_true("output_5", val_5)) {
    return false;
  }

  char testcase_6 = ' ';
  char output_6 = body_to_tail(testcase_6);
  bool val_6 = false;
  if (output_6 == '?'){
    val_6 = true; 
  }
  if (!assert_true("output_6", val_6)) {
    return false;
  } */ 

  return true;
}

bool test_head_to_body() {
  char testChars[] = {'W', 'A', 'S', 'D'};

  for (int i = 0; i < 4; i++)
  {
    char curChar = testChars[i];
    switch (curChar)
    {
    case 'W':
      if (!assert_equals_char("W is not working properly", '^', head_to_body(curChar))) {
        return false;
      }
      break;
    case 'A':
      if (!assert_equals_char("A is not working properly", '<', head_to_body(curChar))) {
        return false;
      }
      break;
    case 'S':
      if (!assert_equals_char("S is not working properly", 'v', head_to_body(curChar))) {
        return false;
      }
      break;
    case 'D':
      if (!assert_equals_char("D is not working properly", '>', head_to_body(curChar))) {
        return false;
      }
      break;
    }
  }
  

  return true;
}

bool test_get_next_row() {
  // TODO: Implement this function.
  char testChars[] = {'^', 'v', 's', 'S', 'w', 'W'};
  
  for (int i = 0; i < 6; i++)
  {
    switch (testChars[i])
    {
    case '^':
    case 'w':
    case 'W':
      if (!assert_equals_int("Did not work for a up moving", 0, (int) get_next_row(1, testChars[i])))
      {
        return false;
      }
      break;
    case 'v':
    case 's':
    case 'S':
      if (!assert_equals_int("Did not work for a down moving", 2, (int) get_next_row(1, testChars[i])))
      {
        return false;
      }
      break;
    }
  }

  return true;
}

bool test_get_next_col() {
  // TODO: Implement this function.
  char testChars[] = {'<', '>', 'a', 'A', 'd', 'D'};
  
  for (int i = 0; i < 6; i++)
  {
    switch (testChars[i])
    {
    case '<':
    case 'a':
    case 'A':
      if (!assert_equals_int("Did not work for a left moving", 0, (int) get_next_col(1, testChars[i])))
      {
        return false;
      }
      break;
    case '>':
    case 'd':
    case 'D':
      if (!assert_equals_int("Did not work for a right moving", 2, (int) get_next_col(1, testChars[i])))
      {
        return false;
      }
      break;
    }
  }

  return true;
}

bool test_customs() {
  if (!test_greater_than_forty_two()) {
    printf("%s\n", "test_greater_than_forty_two failed.");
    return false;
  }
  if (!test_is_vowel()) {
    printf("%s\n", "test_is_vowel failed.");
    return false;
  }
  if (!test_is_tail()) {
    printf("%s\n", "test_is_tail failed");
    return false;
  }
  if (!test_is_head()) {
    printf("%s\n", "test_is_head failed");
    return false;
  }
  if (!test_is_snake()) {
    printf("%s\n", "test_is_snake failed");
    return false;
  }
  if (!test_body_to_tail()) {
    printf("%s\n", "test_body_to_tail failed");
    return false;
  }
  if (!test_head_to_body()) {
    printf("%s\n", "test_head_to_body failed");
    return false;
  }
  if (!test_get_next_row()) {
    printf("%s\n", "test_get_next_row failed");
    return false;
  }
  if (!test_get_next_col()) {
    printf("%s\n", "test_get_next_col failed");
    return false;
  }
  return true;
}

int main(int argc, char* argv[]) {
  init_colors();

  if (!test_and_print("custom", test_customs)) {
    return 0;
  }

  return 0;
}
