# Pawtner Care API

Pawtner Care API is the backend service for a pet care and animal welfare platform. It powers the core features needed by a mobile or web client, including user access, pet listings, donations, events, veterinary clinic records, emergency SOS requests, community content, customer support messaging, and gamification.

The project is built with Spring Boot, uses MySQL for persistence, and exposes REST endpoints for application workflows plus WebSocket support for real-time customer support messaging.

## Project Purpose

This API exists to support a platform where users can:

- create and manage accounts
- log in with a bearer-token based flow
- verify actions through email OTP
- browse and manage pet records
- save favorite pets for later viewing
- discover and manage animal welfare events
- create and contribute to donation campaigns
- manage payment modes for donations
- browse veterinary clinic information
- submit and track emergency SOS cases
- post, comment, and react in a community feed
- chat with customer support in real time
- earn achievements and track user progress through gamification

## Core Functions

### Authentication and Account Access

- User signup and login
- OTP sending and OTP confirmation
- Signup requires a verified `signup` OTP before account creation
- Password reset requires a verified `reset-password` OTP and the OTP code in the final reset request
- Protected endpoints secured with a bearer token filter

Main routes:

- `/api/auth/signup`
- `/api/auth/login`
- `/api/auth/send-otp`
- `/api/auth/confirm-otp`
- `/api/auth/reset-password`

### User Management

- Create, read, update, and delete users
- Retrieve user details for app-level account management

Main routes:

- `/api/users`
- `/api/users/{id}`

### Pet Management

- Create, read, update, and delete pet records
- Support pet listing workflows for the platform
- Let users favorite or unfavorite pets
- Retrieve a user's saved pets

Main routes:

- `/api/pets`
- `/api/pets/{id}`
- `/api/pets/{petId}/favorites`
- `/api/users/{userId}/favorite-pets`

### Events

- Create and manage pet-related or community-related events
- List events for discovery in the client app

Main routes:

- `/api/events`
- `/api/events/{id}`

### Donations

- Create and manage donation campaigns
- Record donation transactions
- Support different campaign types and donation flows
- Manage available payment modes

Main routes:

- `/api/donation-campaigns`
- `/api/donation-campaigns/types`
- `/api/donation-transactions`
- `/api/payment-modes`

### Veterinary Clinics

- Store and manage veterinary clinic information
- Expose clinic listings for user discovery

Main routes:

- `/api/veterinary-clinics`
- `/api/veterinary-clinics/{id}`

### Emergency SOS

- Create and track emergency SOS requests
- Expose SOS types and statuses for the client
- Support rescue or urgent response workflows

Main routes:

- `/api/emergency-sos`
- `/api/emergency-sos/types`
- `/api/emergency-sos/statuses`

### Community Features

- Create, update, and delete community posts
- Add media to posts
- Comment on posts
- Like and unlike posts
- Support hashtags and feed-style content responses

Main routes:

- `/api/community/posts`
- `/api/community/posts/{postId}`
- `/api/community/posts/{postId}/comments`
- `/api/community/posts/{postId}/likes`

### Customer Support

- Open and review support conversations
- Send and receive support messages
- Mark messages as read
- Support both customer and admin support views
- Push real-time updates through WebSocket messaging

Main routes:

- `/api/support/customer/conversation`
- `/api/support/customer/messages`
- `/api/support/customer/read`
- `/api/support/admin/conversations`
- `/api/support/admin/conversations/{conversationId}`
- `/api/support/admin/conversations/{conversationId}/messages`

WebSocket endpoint:

- `/ws/support`

### Gamification

- Record activity events
- Manage achievements from an admin side
- Assign achievements manually
- Seed default achievements
- Show user progress and hero-wall style rankings

Main routes:

- `/api/gamification/events`
- `/api/gamification/heroes-wall`
- `/api/gamification/users/{userId}/achievements`
- `/api/gamification/users/{userId}/progress`
- `/api/gamification/admin/achievements`

## Tech Stack

- Java 17
- Spring Boot 4
- Spring Web MVC
- Spring Data JPA
- Spring Security
- Spring WebSocket
- MySQL
- Lombok
- Maven

## Configuration

The application reads environment variables from `.env` through Spring's optional config import.

Required values include:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `API_BEARER_TOKEN`

Optional JPA settings:

- `SPRING_JPA_HIBERNATE_DDL_AUTO`
- `SPRING_JPA_PROPERTIES_HIBERNATE_FORMAT_SQL`

## Running Locally

1. Create a `.env` file with the required database and auth values.
2. Make sure MySQL is running and the target database exists.
3. Start the API:

```bash
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

## Development Notes

- The API is stateless and uses a custom bearer token authentication filter.
- Most business modules follow a standard Spring structure: `controller`, `service`, `repository`, `dto`, and `entity`.
- Real-time support messaging is implemented with STOMP over WebSocket.

## Package Overview

- `auth` - login, signup, OTP, password reset
- `user` - user records and profile-related operations
- `pet` - pet records and favorites
- `event` - event management
- `donation` - campaigns and donation transactions
- `payment` - payment mode management
- `veterinary` - veterinary clinic management
- `emergency` - emergency SOS handling
- `community` - posts, comments, likes, hashtags, feed responses
- `support` - support chat, conversations, read state, WebSocket events
- `gamification` - achievements, progress tracking, heroes wall

## Status

This repository is set up as the backend API layer for the Pawtner Care platform and already contains the main domain modules needed for a full pet care, rescue, donation, and community experience.
