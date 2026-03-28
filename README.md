# Cricket Scoreboard

A professional Java console application for scoring limited-overs cricket matches with guided manual input, richer cricket rules, intelligent auto-simulation, detailed scorecards, and explainable MVP selection.

## Overview

This project was built to move beyond a basic classroom-style scoreboard and provide a more complete match engine for ODI and T20 cricket.

It supports:

- structured match setup
- menu-driven manual scoring
- automatic match simulation
- advanced dismissal and extras handling
- super over tie-breaks
- player-level batting and bowling statistics
- text scorecard export
- transparent MVP selection with score breakdown and reasons

The application is designed for local execution from the terminal and keeps the scoring experience simple for the user while still supporting realistic cricket match events.

## Highlights

- Guided manual scoring with numbered menus instead of raw event tokens
- ODI and T20 match support with correct over validation
- Advanced wicket types: bowled, caught, run out, stumped, lbw, hit wicket
- Retired out and retired hurt handling
- Wides, no-balls, byes, leg byes, penalty runs, and overthrows
- Free-hit logic after no-balls
- Super over support for tied matches
- Context-aware auto-simulation with more realistic event generation
- Over-by-over summaries and innings summaries
- Text scorecard export to `data/scorecards/`
- Explainable MVP result with batting impact, bowling impact, bonuses, and reasons

## Why This Project Stands Out

This is not just a score counter. The project includes:

- strong scoring flow design
- safer input handling
- more realistic cricket event modelling
- maintainable object-oriented structure
- improved terminal UX
- meaningful end-of-match analytics

It demonstrates practical Java skills in:

- object-oriented design
- input validation
- state management
- domain modelling
- file handling
- structured formatting
- simulation logic

## Core Features

### Match management

- Supports `ODI` and `T20`
- Validates maximum overs by match type
- Supports two innings match flow
- Calculates chase target automatically
- Supports super over for tied results

### Manual scoring UX

Manual scoring uses nested numbered menus so the scorer does not need to remember event abbreviations.

For each ball the user can choose:

1. Batter runs / dot ball
2. Wicket
3. Wide
4. No ball
5. Bye
6. Leg bye
7. Overthrow
8. Penalty runs
9. Retired batter

Submenus then guide the user through:

- run values
- wicket type
- no-ball subtype
- bye and leg-bye counts
- overthrow composition
- retirement type
- bowler selection by number

### Advanced cricket rules

The engine supports:

- bowled
- caught
- run out
- stumped
- lbw
- hit wicket
- retired out
- retired hurt
- wides with multiple runs
- no-ball with bat runs
- no-ball with byes
- no-ball with leg byes
- byes
- leg byes
- penalty runs
- overthrow runs on bat shots
- overthrow runs on byes
- overthrow runs on leg byes
- free-hit restrictions

### Auto simulation

Auto mode is based on innings context rather than flat randomness.

It considers:

- innings phase
- wickets left
- chase pressure
- free-hit state
- attacking intent

It also improves:

- wicket variety
- batting aggression changes
- extras frequency
- bowler rotation

### Statistics and reporting

The application generates:

- live score updates
- over summaries
- innings summaries
- batting event log
- player batting and bowling stats
- final match result
- overall match statistics
- explainable MVP output

### MVP explanation

The MVP system is now structured and transparent.

It evaluates:

- batting impact
- bowling impact
- all-round bonus
- winning bonus
- chase contribution

The final summary explains why the player was selected instead of showing only a name.

## Supported Match Events

### Batting outcomes

- `0`
- `1`
- `2`
- `3`
- `4`
- `6`

### Wickets

- Bowled
- Caught
- Run out
- Stumped
- LBW
- Hit wicket

### Retirements

- Retired out
- Retired hurt

### Extras

- Wide
- No ball
- Bye
- Leg bye
- Penalty runs

### Overthrows

- Bat runs + overthrow runs
- Byes + overthrow runs
- Leg byes + overthrow runs

## Project Structure

```text
src/
  exception/
    InvalidBallInputException.java
  filemanagement/
    MVPResult.java
    ScorecardFileManager.java
    ScorecardFormatter.java
  main/
    CricketScoreboardApplication.java
  match/
    Match.java
    ODIMatch.java
    T20Match.java
  scoreboard/
    DismissalType.java
    InningsScore.java
    ScoreboardManager.java
    WicketEvent.java
  statistics/
    OverStats.java
    Statistics.java
  team/
    Player.java
    Team.java

data/
  scorecards/

build.sh
README.md
```

## Architecture Summary

### `CricketScoreboardApplication`

Application entry point.
Handles startup input such as:

- team names
- player names
- match type
- overs
- batting first choice
- auto mode toggle

### `ScoreboardManager`

Main scoring engine of the project.
Responsible for:

- innings flow
- ball-by-ball processing
- manual scoring menus
- auto-simulation
- over summaries
- match result generation
- scorecard prompts

### `InningsScore`

Stores mutable innings state such as:

- total runs
- wickets
- legal balls
- extras
- striker and non-striker handling
- wicket events

### `Player`

Tracks per-player performance:

- runs
- balls faced
- boundaries
- out status
- wickets taken
- runs conceded

### `ScorecardFormatter`

Formats:

- match result
- innings summaries
- overall statistics
- MVP explanation
- exportable scorecard content

## Build

Use:

```bash
./build.sh
```

This compiles all Java files to both:

- `bin/`
- `out/`

The dual-output build keeps terminal runs and IDE-style runs consistent.

## Run

Recommended:

```bash
java -cp bin main.CricketScoreboardApplication
```

Alternative:

```bash
java -cp out main.CricketScoreboardApplication
```

## Example Flow

Typical application flow:

1. Enter both team names
2. Enter 11 players for each team
3. Select match type
4. Select overs
5. Choose batting first
6. Choose manual mode or auto mode
7. Score the match innings by innings
8. View final summary and MVP
9. Optionally save the scorecard as a text file

## Scorecard Export

At match end, the application can save:

- first innings scorecard
- second innings scorecard
- both innings

Saved files are written to:

```text
data/scorecards/
```

## MVP Output Example

The MVP section now looks like this:

```text
MVP (Player of the Match): PlayerName
Runs: 37
Balls: 9
Strike Rate: 411.11
Wickets: 0
Runs Conceded: 0
Batting Impact: 104.03
Bowling Impact: 0.00
All-Round Bonus: 0.00
Winning Bonus: 16.00
Final MVP Score: 120.03
Reason:
- Strong batting contribution
- Explosive strike rate
- Large share of team total
- Finished unbeaten
- Top scorer for winning team
- Winning team impact bonus
```

## Technical Strengths

- Clear object-oriented model
- Better-than-basic cricket rule handling
- Input flow designed for real users
- Extendable scoring architecture
- Practical file export support
- Explainable analytics output

## Current Limitations

- Console application only
- No database or persistent match history
- No JSON or CSV export yet
- Bowling analytics can still be expanded further
- No automated unit test suite yet

## Future Scope

- JSON and CSV exports
- richer bowling figures
- fall of wickets summary
- partnership tracking
- required run rate display
- test coverage
- CI integration
- GUI or web version

## Resume Value

This project is suitable to present in a portfolio or resume because it demonstrates:

- Java programming
- domain-driven problem solving
- structured state management
- input-system design
- simulation modelling
- file handling
- formatting and reporting
- incremental feature evolution in an existing codebase

## Final Note

This project is now a feature-rich cricket scoreboard with a stronger match engine, better user experience, improved simulation, and professional end-of-match reporting in a terminal-based Java application.
