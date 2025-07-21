package validation

import "github.com/go-playground/validator/v10"

var Validator *validator.Validate

func Init() {
    Validator = validator.New(validator.WithRequiredStructEnabled())
}
