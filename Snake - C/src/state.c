#include "state.h"

#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "snake_utils.h"

/* Helper function definitions */
static void set_board_at(game_state_t* state, unsigned int row, unsigned int col, char ch);
static bool is_tail(char c);
static bool is_head(char c);
static bool is_snake(char c);
static char body_to_tail(char c);
static char head_to_body(char c);
static unsigned int get_next_row(unsigned int cur_row, char c);
static unsigned int get_next_col(unsigned int cur_col, char c);
static void find_head(game_state_t* state, unsigned int snum);
static char next_square(game_state_t* state, unsigned int snum);
static void update_tail(game_state_t* state, unsigned int snum);
static void update_head(game_state_t* state, unsigned int snum);

/* Task 1 */
game_state_t* create_default_state() {
  // TODO: Implement this function.
  game_state_t *game = (game_state_t*) malloc(sizeof(game_state_t));
  game->num_rows = 18;
  game->board = malloc(18 * sizeof(char*));

  for (int i = 0; i < game->num_rows; i++)
  {
    game->board[i] = malloc(21 * sizeof(char));
    if (i == 0 || i == 17)
    {
      for (int j = 0; j < 20; j++)
      {
        game->board[i][j] = '#';
      }
      game->board[i][20] = '\0';
    } else {
      
      for (int j = 0; j < 20; j++)
      {
        if (i == 2 && j == 2) {
          game->board[i][j] = 'd';
        }
        else if (i == 2 && j == 3) {
          game->board[i][j] = '>';
        }
        else if (i == 2 && j == 4) {
          game->board[i][j] = 'D';
        }
        else if (i == 2 && j == 9) {
          game->board[i][j] = '*';
        }
        else if (j == 0 || j == 19)
        {
          game->board[i][j] = '#';
        } else {
          game->board[i][j] = ' ';
        } 
      }
      game->board[i][20] = '\0';
     }
    }

  game->num_snakes = 1;  
  snake_t* snake_one = malloc(sizeof(snake_t)); 
  snake_one->tail_row = 2; 
  snake_one->tail_col = 2; 
  snake_one->head_col = 4; 
  snake_one->head_row = 2;
  snake_one->live = true; 
  
  game->snakes = snake_one;
  return game; 
}

/* Task 2 */
void free_state(game_state_t* state) {
  for (int i = 0; i < state->num_rows; i++) { //Free every board value
    free(state->board[i]);
  }
  free(state->board);
  free(state->snakes);
  free(state); 
  return;
}

/* Task 3 */
void print_board(game_state_t* state, FILE* fp) {
  for (int row = 0; row < state->num_rows; row++) {
    fprintf(fp,"%s\n", state->board[row]); 
  } 
  return;
}

/*
  Saves the current state into filename. Does not modify the state object.
  (already implemented for you).
*/
void save_board(game_state_t* state, char* filename) {
  FILE* f = fopen(filename, "w");
  print_board(state, f);
  fclose(f);
}

/* Task 4.1 */

/*
  Helper function to get a character from the board
  (already implemented for you).
*/
char get_board_at(game_state_t* state, unsigned int row, unsigned int col) {
  return state->board[row][col];
}

/*
  Helper function to set a character on the board
  (already implemented for you).
*/
static void set_board_at(game_state_t* state, unsigned int row, unsigned int col, char ch) {
  state->board[row][col] = ch;
}

/*
  Returns true if c is part of the snake's tail.
  The snake consists of these characters: "wasd"
  Returns false otherwise.
*/
static bool is_tail(char c) {
  // TODO: Implement this function.
  if (c == 'w' || c == 'a' || c == 's' || c == 'd') {
    return true;
  }
  return false;
}

/*
  Returns true if c is part of the snake's head.
  The snake consists of these characters: "WASDx"
  Returns false otherwise.
*/
static bool is_head(char c) {
  // TODO: Implement this function.
  if (c == 'W' || c == 'A' || c == 'S' || c == 'D') {
    return true;
  }
  return false;
}

/*
  Returns true if c is part of the snake.
  The snake consists of these characters: "wasd^<v>WASDx"
*/
static bool is_snake(char c) {
  char *checking_s; 
  char in_snake[] = "wasd^<v>WASDx"; 

  checking_s = strchr(in_snake, c); 

  if (checking_s == NULL) {
    return false;
  }
  // TODO: Implement this function.
  return true;
}

/*
  Converts a character in the snake's body ("^<v>")
  to the matching character representing the snake's
  tail ("wasd").
*/
static char body_to_tail(char c) {
  // TODO: Implement this function.
  switch (c)
  {
  case '^':
    return 'w';
  case '<':
    return 'a';
  case 'v':
    return 's';
  case '>':
    return 'd';
  default:
    return '?';
  }
  //return '?';
}

/*
  Converts a character in the snake's head ("WASD")
  to the matching character representing the snake's
  body ("^<v>").
*/
static char head_to_body(char c) {
  // TODO: Implement this function.
  //return '?';
  switch (c)
  {
  case 'W':
    return '^';
  case 'A':
    return '<';
  case 'S':
    return 'v';
  case 'D':
    return '>';
  default:
    return '?';
  }
}

/*
  Returns cur_row + 1 if c is 'v' or 's' or 'S'.
  Returns cur_row - 1 if c is '^' or 'w' or 'W'.
  Returns cur_row otherwise.
*/
static unsigned int get_next_row(unsigned int cur_row, char c) {
  // TODO: Implement this function.
  switch (c)
  {
  case 'v':
  case 's':
  case 'S':
    return cur_row + 1;
  case '^':
  case 'w':
  case 'W':
    return cur_row - 1;
  default:
    return cur_row;
  }
  //return cur_row;
}

/*
  Returns cur_col + 1 if c is '>' or 'd' or 'D'.
  Returns cur_col - 1 if c is '<' or 'a' or 'A'.
  Returns cur_col otherwise.
*/
static unsigned int get_next_col(unsigned int cur_col, char c) {
  // TODO: Implement this function.
  switch (c)
  {
  case '>':
  case 'd':
  case 'D':
    return cur_col + 1;
  case '<':
  case 'a':
  case 'A':
    return cur_col - 1;
  default:
    return cur_col;
  }
}

/*
  Task 4.2

  Helper function for update_state. Return the character in the cell the snake is moving into.

  This function should not modify anything.
*/
static char next_square(game_state_t* state, unsigned int snum) {
  // TODO: Implement this function.
  unsigned int headCol = state->snakes[snum].head_col; 
  unsigned int headRow = state->snakes[snum].head_row; 

  char headChar = get_board_at(state, headRow, headCol); 

  switch (headChar) {
    case 'W': 
      return get_board_at(state, get_next_row(headRow, headChar), headCol); 
    case 'S': 
      return get_board_at(state, get_next_row(headRow, headChar), headCol);
    case 'D': 
      return get_board_at(state, headRow, get_next_col(headCol, headChar));
    case 'A': 
      return get_board_at(state, headRow, get_next_col(headCol, headChar));
  }
  return '?';
}

/*
  Task 4.3

  Helper function for update_state. Update the head...

  ...on the board: add a character where the snake is moving

  ...in the snake struct: update the row and col of the head

  Note that this function ignores food, walls, and snake bodies when moving the head.
*/
static void update_head(game_state_t* state, unsigned int snum) {
  // TODO: Implement this function.
  unsigned int headCol = state->snakes[snum].head_col; 
  unsigned int headRow = state->snakes[snum].head_row; 
  char headChar = get_board_at(state, headRow, headCol); 
  unsigned int newRow = get_next_row(headRow, headChar); 
  unsigned int newCol = get_next_col(headCol, headChar);
  state->board[newRow][newCol] = headChar; 
  state->board[headRow][headCol] = head_to_body(headChar);
  state->snakes[snum].head_col = newCol;
  state->snakes[snum].head_row = newRow; 
  return;
}

/*
  Task 4.4

  Helper function for update_state. Update the tail...

  ...on the board: blank out the current tail, and change the new
  tail from a body character (^<v>) into a tail character (wasd)

  ...in the snake struct: update the row and col of the tail
*/
static void update_tail(game_state_t* state, unsigned int snum) {
  // TODO: Implement this function.
  unsigned int tailCol = state->snakes[snum].tail_col; 
  unsigned int tailRow = state->snakes[snum].tail_row; 
  char tailChar = get_board_at(state, tailRow, tailCol); 
  unsigned int newRow = get_next_row(tailRow, tailChar); 
  unsigned int newCol = get_next_col(tailCol, tailChar);
  state->board[newRow][newCol] = body_to_tail(get_board_at(state, newRow, newCol));
  state->board[tailRow][tailCol] = ' '; 
  state->snakes[snum].tail_col = newCol;
  state->snakes[snum].tail_row = newRow; 
  return;
}

/* Task 4.5 */
void update_state(game_state_t* state, int (*add_food)(game_state_t* state)) {
  // TODO: Implement this function.
  for (unsigned int sn = 0; sn < state->num_snakes; sn++) {
    char next_move = next_square(state, sn); 
    unsigned int headCol = state->snakes[sn].head_col; 
    unsigned int headRow = state->snakes[sn].head_row;
    snake_t *snake = &(state->snakes[sn]);  
    if (next_move == '#' || is_snake(next_move)) {
      state->board[headRow][headCol] = 'x'; 
      snake->live = false; 
    } else if (next_move == '*'){
      update_head(state, sn); 
      add_food(state); 
    } else {
      update_head(state, sn);
      update_tail(state, sn); 
    }

  } 
  
  
  
  return;
}

/* Task 5 */
game_state_t* load_board(char* filename) {
  // TODO: Implement this function.
  FILE* board_file = fopen(filename, "r");
  game_state_t *game = (game_state_t*) malloc(sizeof(game_state_t));
  char** board = (char**) malloc(1 * sizeof(char*));
  unsigned int numRows = 0; 
  unsigned int curCol = 0; 
  board[0] = malloc(1 * sizeof(char)); 
  for (char square = (char) fgetc(board_file); square != EOF; square = (char) fgetc(board_file)){
    char newSquare = square; 
    unsigned int oneMore = (curCol + 1);
    board[numRows] = (char*) realloc(board[numRows], oneMore * sizeof(char)); 
    if (newSquare == '\n') {
      board[numRows][curCol] = '\0';
      numRows++; 
      board = (char**) realloc(board, sizeof(char*) * (numRows + 1)); 
      board[numRows] = malloc(sizeof(char));
      curCol = 0; 
    } else {
      board[numRows][curCol] = newSquare;
      curCol++; 
    }
  }
  board = (char**) realloc(board, sizeof(char*) * (numRows)); 
  game->num_snakes = 0; 
  game->snakes = NULL; 
  game->num_rows = numRows; 
  game->board = board; 
  fclose(board_file); 
  return game;
}

/*
  Task 6.1

  Helper function for initialize_snakes.
  Given a snake struct with the tail row and col filled in,
  trace through the board to find the head row and col, and
  fill in the head row and col in the struct.
*/
static void find_head(game_state_t* state, unsigned int snum) {
  // TODO: Implement this function.
  snake_t* tempSnake = &(state->snakes[snum]); 
  unsigned int curCol = tempSnake->tail_col; 
  unsigned int curRow = tempSnake->tail_row; 
  char curChar = get_board_at(state, curRow, curCol); 
  while (!is_head(curChar)) {
    curCol = get_next_col(curCol, curChar); 
    curRow = get_next_row(curRow, curChar); 
    curChar = get_board_at(state, curRow, curCol); 
  }
  tempSnake->head_col = curCol;
  tempSnake->head_row = curRow;  
  return;
}

/* Task 6.2 */
game_state_t* initialize_snakes(game_state_t* state) {
  // TODO: Implement this function.
  state->num_snakes = 0; 
  state->snakes = malloc(sizeof(1));
  for (unsigned int row = 0; row < state->num_rows; row++) {
    for(unsigned int col = 0; col < strlen(state->board[row]); col++) {
      char curChar = get_board_at(state, row, col); 
      if (is_tail(curChar)) {
        state->num_snakes++; 
        state->snakes = realloc(state->snakes, sizeof(snake_t) * (state->num_snakes)); 
        //snake_t* tempSnake = malloc(sizeof(snake_t)); 
        snake_t tempSnake = {row, col, 5, 10, true};
        state->snakes[state->num_snakes - 1] = tempSnake;
        find_head(state, state->num_snakes - 1); 
      }
    }
  }
  return state; 
  
}
